package org.max2ba.usernotificationservcei.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.max2ba.usernotificationservcei.service.EmailService;
import org.springframework.stereotype.Service;
import org.max2ba.notificationservice.openapi.model.SendMessageResponse;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Override
    public SendMessageResponse sendEmail(SendEmailRequest request) {
        log.info(request.getUserOperation().toString()+":"+request.getEmail());
        return new SendMessageResponse("Отправлено", request.getEmail());
    }
}
