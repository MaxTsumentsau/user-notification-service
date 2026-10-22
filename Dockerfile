FROM quay.io/debezium/connect:2.7.4.Final

USER root

RUN mkdir -p /kafka/connect/confluent

RUN cd /kafka/connect/confluent && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-connect-avro-converter/7.7.0/kafka-connect-avro-converter-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-connect-avro-data/7.7.0/kafka-connect-avro-data-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-schema-converter/7.7.0/kafka-schema-converter-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-avro-serializer/7.7.0/kafka-avro-serializer-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-schema-serializer/7.7.0/kafka-schema-serializer-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/kafka-schema-registry-client/7.7.0/kafka-schema-registry-client-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/common-config/7.7.0/common-config-7.7.0.jar && \
    curl -L -O https://packages.confluent.io/maven/io/confluent/common-utils/7.7.0/common-utils-7.7.0.jar && \
    curl -L -O https://repo1.maven.org/maven2/org/apache/avro/avro/1.11.3/avro-1.11.3.jar && \
    curl -L -O https://repo1.maven.org/maven2/com/google/guava/guava/32.0.1-jre/guava-32.0.1-jre.jar && \
    curl -L -O https://repo1.maven.org/maven2/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar

USER kafka