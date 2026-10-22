package org.max2ba.usernotificationservcei.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
public class KafkaTopicConfig {

    public static final String USER_SERVICE_PUBLIC_USERS = "user-service.public.users";
    public static final String USER_EVENT = "user-events";

    @Bean
    @Profile("!test")
    public NewTopic PublicUsersTopic() {
        return TopicBuilder.name(USER_SERVICE_PUBLIC_USERS)
                .partitions(4)
                .replicas(2)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .build();
    }

    @Bean
    @Profile("!test")
    public NewTopic UserEventsTopic() {
        return TopicBuilder.name(USER_EVENT)
                .partitions(4)
                .replicas(2)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .build();
    }
}
