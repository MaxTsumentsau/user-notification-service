package org.max2ba.usernotificationservcei.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ValueMapping;
import org.max2ba.usernotificationservcei.proto.UserNotificationRequest;
import org.max2ba.usernotificationservcei.proto.UserNotificationResponse;
import org.max2ba.notificationservice.openapi.model.SendEmailRequest;
import org.max2ba.notificationservice.openapi.model.SendMessageResponse;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @ValueMapping(source = "USER_OPERATION_UNSPECIFIED", target = MappingConstants.THROW_EXCEPTION)
    @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.THROW_EXCEPTION)
    @ValueMapping(source = "CREATE", target = "CREATE")
    @ValueMapping(source = "DELETE", target = "DELETE")
    SendEmailRequest toEmailRequest(UserNotificationRequest request);

    UserNotificationResponse toProtoResponse(SendMessageResponse response);
}
