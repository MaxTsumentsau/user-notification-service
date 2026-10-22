package org.max2ba.usernotificationservcei.kafka;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import org.max2ba.usernotificationservcei.avro.UserEvent;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SerdeConfig {

    private static final String SCHEMA_REGISTRY_URL_KEY = "schema.registry.url";
    private final KafkaProperties kafkaProperties;

    @Bean
    public GenericAvroSerde genericAvroSerde() {
        GenericAvroSerde serde = new GenericAvroSerde();
        Map<String, Object> config = new HashMap<>();
        config.put(SCHEMA_REGISTRY_URL_KEY, kafkaProperties.getProperties().get(SCHEMA_REGISTRY_URL_KEY));
        serde.configure(config, false);
        return serde;
    }

    @Bean
    public SpecificAvroSerde<UserEvent> specificAvroSerde() {
        SpecificAvroSerde<UserEvent> serde = new SpecificAvroSerde<>();
        Map<String, Object> config = new HashMap<>();
        config.put(SCHEMA_REGISTRY_URL_KEY, kafkaProperties.getProperties().get(SCHEMA_REGISTRY_URL_KEY));
        serde.configure(config, false);
        return serde;
    }
}
