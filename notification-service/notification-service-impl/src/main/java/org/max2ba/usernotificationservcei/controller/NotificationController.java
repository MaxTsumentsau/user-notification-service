package org.max2ba.usernotificationservcei.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.max2ba.usernotificationservcei.service.EmailService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.max2ba.notificationservice.openapi.model.SendMessageResponse;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;
import org.max2ba.notificationservice.openapi.api.NotificationsApi;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationsApi {

    private final EmailService emailService;

    @PostMapping
    public SendMessageResponse sendEmail(@Valid @RequestBody SendEmailRequest request) {
        return emailService.sendEmail(request);
    }
}
