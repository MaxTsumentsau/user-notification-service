package org.max2ba.usernotificationservcei.service;

import org.springframework.data.domain.Page;
import org.max2ba.userservice.openapi.model.UserDto;
import org.max2ba.userservice.openapi.model.UserRequest;

import java.util.UUID;

public interface UserService {
    UserDto createUser(UserRequest request);

    UserDto updateUser(UUID id, UserRequest request);

    UserDto getUser(UUID id);

    Page<UserDto> searchUsers(String name, int page, int size);

    UserDto deleteUser(UUID id);
}
