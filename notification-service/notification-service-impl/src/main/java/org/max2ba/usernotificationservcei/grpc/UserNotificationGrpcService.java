package org.max2ba.usernotificationservcei.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.max2ba.usernotificationservcei.mapper.NotificationMapper;
import org.max2ba.usernotificationservcei.proto.UserNotificationRequest;
import org.max2ba.usernotificationservcei.proto.UserNotificationResponse;
import org.max2ba.usernotificationservcei.proto.UserNotificationServiceGrpc;
import org.max2ba.usernotificationservcei.service.EmailService;

@GrpcService
@RequiredArgsConstructor
public class UserNotificationGrpcService extends UserNotificationServiceGrpc.UserNotificationServiceImplBase {

    private final EmailService emailService;
    private final NotificationMapper mapper;

    @Override
    public void sendUserNotification(UserNotificationRequest request, StreamObserver<UserNotificationResponse> responseObserver) {
        try {
            var result = emailService.sendEmail(mapper.toEmailRequest(request));
            responseObserver.onNext(mapper.toProtoResponse(result));
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription(e.getMessage())
                    .withCause(e)
                    .asRuntimeException());
        }
    }
}