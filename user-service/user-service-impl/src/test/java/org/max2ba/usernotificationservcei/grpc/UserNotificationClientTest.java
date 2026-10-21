package org.max2ba.usernotificationservcei.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.max2ba.usernotificationservcei.proto.UserNotificationRequest;
import org.max2ba.usernotificationservcei.proto.UserNotificationResponse;
import org.max2ba.usernotificationservcei.proto.UserNotificationServiceGrpc;
import org.max2ba.usernotificationservcei.proto.UserOperation;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class UserNotificationClientTest {

    @Mock
    private UserNotificationServiceGrpc.UserNotificationServiceFutureStub stub;

    @InjectMocks
    private UserNotificationClient client;

    @Test
    void notify_shouldSendCorrectRequestAndReturnResponse() throws Exception {
        var expected = UserNotificationResponse.newBuilder()
                .setMessage("OK")
                .setDetails("done")
                .build();

        when(stub.sendUserNotification(any(UserNotificationRequest.class)))
                .thenReturn(Futures.immediateFuture(expected));

        var future = client.notify(UserOperation.CREATE, "test@example.com");

        var response = future.get(5, TimeUnit.SECONDS);
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getDetails()).isEqualTo("done");

        var captor = ArgumentCaptor.forClass(UserNotificationRequest.class);
        verify(stub).sendUserNotification(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(captor.getValue().getUserOperation()).isEqualTo(UserOperation.CREATE);
    }

    @Test
    void notify_whenStubFails_shouldCompleteExceptionally() {
        when(stub.sendUserNotification(any(UserNotificationRequest.class)))
                .thenReturn(Futures.immediateFailedFuture(
                        new RuntimeException("SMTP server down")));

        var future = client.notify(UserOperation.DELETE, "fail@example.com");

        assertThatThrownBy(() -> future.get(5, TimeUnit.SECONDS))
                .hasCauseInstanceOf(RuntimeException.class)
                .hasMessageContaining("SMTP server down");
    }
}