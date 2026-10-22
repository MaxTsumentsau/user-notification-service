package org.max2ba.usernotificationservcei.kafka;

import io.confluent.kafka.streams.serdes.avro.GenericAvroSerde;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.max2ba.usernotificationservcei.avro.UserEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SerdeConfig {

    private static final String SCHEMA_REGISTRY_URL = "http://localhost:8081";

    @Bean
    public GenericAvroSerde genericAvroSerde() {
        GenericAvroSerde serde = new GenericAvroSerde();
        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        serde.configure(config, false);
        return serde;
    }

    @Bean
    public SpecificAvroSerde<UserEvent> specificAvroSerde() {
        SpecificAvroSerde<UserEvent> serde = new SpecificAvroSerde<>();
        Map<String, Object> config = new HashMap<>();
        config.put("schema.registry.url", SCHEMA_REGISTRY_URL);
        serde.configure(config, false);
        return serde;
    }
}
