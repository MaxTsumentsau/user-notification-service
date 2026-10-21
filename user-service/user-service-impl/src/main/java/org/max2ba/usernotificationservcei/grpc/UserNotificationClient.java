package org.max2ba.usernotificationservcei.grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import net.javacrumbs.futureconverter.java8guava.FutureConverter;
import org.max2ba.usernotificationservcei.proto.UserNotificationRequest;
import org.max2ba.usernotificationservcei.proto.UserNotificationResponse;
import org.max2ba.usernotificationservcei.proto.UserNotificationServiceGrpc;
import org.max2ba.usernotificationservcei.proto.UserOperation;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class UserNotificationClient {

    @GrpcClient("notification-service")
    private UserNotificationServiceGrpc.UserNotificationServiceFutureStub stub;

    public CompletableFuture<UserNotificationResponse> notify(UserOperation operation, String email) {
        return FutureConverter.toCompletableFuture(stub.sendUserNotification(UserNotificationRequest.newBuilder()
                .setUserOperation(operation)
                .setEmail(email)
                .build()));
    }
}
