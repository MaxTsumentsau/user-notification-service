package org.max2ba.usernotificationservcei.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.max2ba.usernotificationservcei.avro.UserEvent;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@EnableKafka
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ObjectMapper objectMapper;

    //@RetryableTopic
    @KafkaListener(topics = "user-events", groupId = "notification-service-group")
    public void handleTransferResult(UserEvent event) {
        try {
            log.info(event.getEmail()+": " + event.getOperation().toString());
        } catch (IllegalArgumentException e) {
            log.error("Failed to convert message: {}", e.getMessage());
            //TODO Можно отправить в DLQ
        }
    }

//    @DltHandler
//    public void handleDltMessage(ConsumerRecord<String, Object> message) {
//        //TODO deadLetterService.save(message);
//        //TODO send event to notification service
//    }
}
