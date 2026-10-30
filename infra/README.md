# Инфраструктура user-notification-service

Манифесты для развёртывания стека в OpenShift CRC (namespace `my-java-app` и `kafka`).

## Состав

| Компонент | Namespace | Назначение |
|---|---|---|
| Postgres 18.1 | `my-java-app` | БД `user_service_db`, источник CDC |
| Kafka 4.3.1 (KRaft) | `kafka` | Strimzi, NodePool `dual-role` (controller+broker) |
| KafkaTopic | `kafka` | `user-service.public.users`, `user-events` |
| Schema Registry 7.7.12 | `my-java-app` | Avro schemas для Kafka Connect |
| Kafka Connect | `kafka` | Strimzi, воркер |
| Debezium Connector | `kafka` | PostgreSQL CDC → Kafka |
| AkhQ 0.28.0 | `my-java-app` | Kafka UI |
| Nexus 3.68.0 | `my-java-app` | Maven repository (для CI/CD) |

## Порядок применения

```bash
# 1. Namespace (уже существуют, apply безопасен)
oc apply -f infra/00-namespaces.yaml

# 2. Kafka (cluster + node pool + topics)
oc apply -f infra/02-kafka/nodepool.yaml
oc apply -f infra/02-kafka/kafka.yaml
oc wait kafka/my-cluster -n kafka --for=condition=Ready --timeout=600s
oc apply -f infra/02-kafka/topics.yaml

# 3. Postgres
oc apply -f infra/01-postgres/pvc.yaml
oc apply -f infra/01-postgres/service.yaml
oc apply -f infra/01-postgres/deployment.yaml
oc rollout status deployment/postgres -n my-java-app --timeout=120s

# 4. Schema Registry
oc apply -f infra/03-schema-registry/imagestream.yaml
oc apply -f infra/03-schema-registry/service.yaml
oc apply -f infra/03-schema-registry/deployment.yaml
oc rollout status deployment/schema-registry -n my-java-app --timeout=120s

# 5. Kafka Connect
oc apply -f infra/connect/kafka-connect.yaml
oc wait kafkaconnect/my-connect-cluster -n kafka --for=condition=Ready --timeout=600s

# 6. Debezium Connector
oc apply -f infra/connect/debezium-connector.yaml

# 7. AkhQ
oc apply -f infra/04-akhq/configmap.yaml
oc apply -f infra/04-akhq/imagestream.yaml
oc apply -f infra/04-akhq/service.yaml
oc apply -f infra/04-akhq/deployment.yaml

# 8. Nexus
oc apply -f infra/05-nexus/nexus.yaml
```

## Пост-установочный шаг: publication для Debezium

Debezium в режиме `pgoutput` требует **существующую publication** на таблицу `public.users`. Publication **не создаётся автоматически** — ни Debezium, ни init-скриптом Postgres (потому что таблица создаётся миграциями приложения позже, чем стартует Postgres).

После **первого запуска** `user-service` (когда миграции создали таблицу `public.users`) выполнить **один раз**:

```bash
oc exec -n my-java-app deploy/postgres -- psql -U postgres -d user_service_db -c "CREATE PUBLICATION dbz_users_publication FOR TABLE public.users;"
```

Проверить:

```bash
oc exec -n my-java-app deploy/postgres -- psql -U postgres -d user_service_db -c "SELECT pubname FROM pg_publication;"
```

Слот `debezium_users_slot` Debezium создаёт сам при старте — вручную создавать **не надо** (иначе `replication slot already exists`).

## Known debt / Known issues

### 1. Пароли в открытом виде

`infra/01-postgres/deployment.yaml` (`POSTGRES_PASSWORD: qwerty`) и `infra/connect/debezium-connector.yaml` (`database.password: qwerty`) содержат пароль в открытом виде. **Планируется вынести в HashiCorp Vault** (или OpenShift Secret + `FileConfigProvider`). До этого репозиторий **должен быть приватным**.

### 2. AkhQ Connect view не работает

В `infra/04-akhq/configmap.yaml` указан `connect.url: http://user-service:8083`, но:
- У сервиса `user-service` **нет порта 8083** (только 8080).
- Правильный адрес Connect API — `my-connect-cluster-connect-api.kafka.svc:8083`, но он **заблокирован NetworkPolicy** Strimzi для подов из `my-java-app`.

Раздел «Connect» в AkhQ пуст. На CDC-пайплайн не влияет. Чтобы починить:
1. Заменить URL на `http://my-connect-cluster-connect-api.kafka.svc:8083`.
2. Создать NetworkPolicy в `kafka`, разрешающую ingress на 8083 от подов `app: akhq` из `my-java-app`.

### 3. Avro Converter собран из jar вручную

В `infra/connect/kafka-connect.yaml` плагин `kafka-connect-avro-converter` собран из отдельных jar-файлов Confluent 7.7.0. Это работает, но хрупко: легко пропустить транзитивную зависимость. Альтернатива — использовать Confluent Hub zip, если он появится.

### 4. Ресурсы заданы не везде

`resources` (requests/limits) заданы для: Kafka (NodePool), Kafka Connect, Nexus, Schema Registry, AkhQ (только requests), Postgres (только requests). **Осознанное решение:** на CRC мало памяти, жёсткие `limits` привели бы к OOMKilled. Postgres, Schema Registry, AkhQ могут быть вытеснены (eviction) при нехватке памяти на узле — данные не теряются (PVC/состояние в Kafka).

### 5. PVC: запрошено 5Gi, выделено 79Gi

На CRC `crc-csi-hostpath-provisioner` игнорирует `requests.storage` и всегда выделяет 79Gi (дефолт). В манифестах указано `5Gi` (желаемый минимум для переносимости). На другом кластере с нормальным provisioner'ом выделится ровно 5Gi.

### 6. `deleteClaim: true` у Kafka NodePool

При удалении `KafkaNodePool` PVC с данными Kafka тоже удалится. Для тестового CRC — приемлемо, данные Kafka не критичны. Для прода — поменять на `false`.

## Проверка состояния

```bash
# Kafka
oc get kafka,kafkatopic,kafkaconnect,kafkaconnector,kafkanodepool -n kafka

# Postgres, Schema Registry, AkhQ, Nexus
oc get deployment,service,pvc,imagestream,route -n my-java-app

# Топики в Kafka
oc rsh -n kafka my-cluster-dual-role-0 bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## Что НЕ входит в этот репозиторий

- **Jenkins** (`jenkins-fixed` в `my-java-app`) — CI-слой, управляется отдельно.
- **`user-service` / `notification-service`** — собираются через S2I (`oc start-build`) из Jenkins Pipeline, не являются инфраструктурой.
- **Операторы** (Strimzi, OpenShift Pipelines) — устанавливаются через OLM/Subscription, отдельный слой.
- **Секреты** — пока в манифестах (см. Known debt #1), в планах — Vault.