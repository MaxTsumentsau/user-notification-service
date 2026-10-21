package org.max2ba.usernotificationservcei.service;
import org.max2ba.notificationservice.openapi.model.SendMessageResponse;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;

public interface EmailService {
    SendMessageResponse sendEmail(SendEmailRequest request);
}
