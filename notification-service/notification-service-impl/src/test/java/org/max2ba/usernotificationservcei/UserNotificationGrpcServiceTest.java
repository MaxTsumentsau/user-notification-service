package org.max2ba.usernotificationservcei;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;
import org.max2ba.notificationservice.openapi.model.SendMessageResponse;
import org.max2ba.usernotificationservcei.grpc.UserNotificationGrpcService;
import org.max2ba.usernotificationservcei.mapper.NotificationMapper;
import org.max2ba.usernotificationservcei.proto.UserNotificationRequest;
import org.max2ba.usernotificationservcei.proto.UserNotificationServiceGrpc;
import org.max2ba.usernotificationservcei.proto.UserOperation;
import org.max2ba.usernotificationservcei.service.EmailService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class UserNotificationGrpcServiceTest {

    @Mock
    private EmailService emailService;

    private Server server;
    private ManagedChannel channel;
    private UserNotificationServiceGrpc.UserNotificationServiceBlockingStub blockingStub;

    @BeforeEach
    void setUp() throws IOException {
        var mapper = Mappers.getMapper(NotificationMapper.class);

        var service = new UserNotificationGrpcService(emailService, mapper);

        var serverName = InProcessServerBuilder.generateName();

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(service)
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        blockingStub = UserNotificationServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void sendUserNotification_shouldReturnMappedResponse() {
        var mockResponse = new SendMessageResponse()
                .message("Отправлено")
                .details("Пользователь с id 123 удален");
        when(emailService.sendEmail(any(SendEmailRequest.class))).thenReturn(mockResponse);

        var request = UserNotificationRequest.newBuilder()
                .setUserOperation(UserOperation.CREATE)
                .setEmail("test@example.com")
                .build();

        var response = blockingStub.sendUserNotification(request);

        assertThat(response.getMessage()).isEqualTo("Отправлено");
        assertThat(response.getDetails()).isEqualTo("Пользователь с id 123 удален");
    }

    @Test
    void sendUserNotification_whenEmailServiceThrows_shouldReturnInternalError() {
        when(emailService.sendEmail(any(SendEmailRequest.class)))
                .thenThrow(new RuntimeException("SMTP server down"));

        var request = UserNotificationRequest.newBuilder()
                .setUserOperation(UserOperation.CREATE)
                .setEmail("test@example.com")
                .build();

        assertThatThrownBy(() -> blockingStub.sendUserNotification(request))
                .isInstanceOf(StatusRuntimeException.class)
                .satisfies(ex -> {
                    StatusRuntimeException sre = (StatusRuntimeException) ex;
                    assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
                    assertThat(sre.getStatus().getDescription()).contains("SMTP server down");
                });
    }
}
