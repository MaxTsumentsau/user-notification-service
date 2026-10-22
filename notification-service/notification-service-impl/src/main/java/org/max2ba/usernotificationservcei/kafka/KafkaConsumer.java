package org.max2ba.usernotificationservcei.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;
import org.max2ba.notificationservice.openapi.model.UserOperation;
import org.max2ba.usernotificationservcei.avro.UserEvent;
import org.max2ba.usernotificationservcei.service.EmailService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${app.kafka.topics.user-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleTransferResult(UserEvent event) {
        try {
            log.trace("Get message from kafka: {}, {}", event.getEmail(), event.getOperation().toString());
            emailService.sendEmail(SendEmailRequest.builder()
                    .email(event.getEmail().toString())
                    .userOperation(UserOperation.valueOf(event.getOperation().toString()))
                    .build());
        } catch (Exception e) {
            log.error("Failed to convert message: {}", e.getMessage());
        }
    }
}
