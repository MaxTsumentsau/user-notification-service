package org.max2ba.usernotificationservcei.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.TopicBuilder;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfig {

    private final KafkaTopicsProperties topics;

    @Bean
    @Profile("!test")
    public NewTopic PublicUsersTopic() {
        return TopicBuilder.name(topics.getUsersSource())
                .partitions(4)
                .replicas(2)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .build();
    }

    @Bean
    @Profile("!test")
    public NewTopic UserEventsTopic() {
        return TopicBuilder.name(topics.getUserEvents())
                .partitions(4)
                .replicas(2)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, "delete")
                .config(TopicConfig.RETENTION_MS_CONFIG, "86400000")
                .build();
    }
}
