package org.max2ba.usernotificationservcei.mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.max2ba.usernotificationservcei.model.User;
import org.max2ba.userservice.openapi.model.UserDto;
import org.max2ba.userservice.openapi.model.UserRequest;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    User toUser(UserRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateUserFromRequest(UserRequest request, @MappingTarget User user);
}
