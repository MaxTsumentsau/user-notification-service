package org.max2ba.usernotificationservcei.service.impl;

import static org.max2ba.usernotificationservcei.constant.UserConstants.USER_NOT_FOUND_EXCEPTION;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.max2ba.usernotificationservcei.grpc.UserNotificationClient;
import org.max2ba.usernotificationservcei.mapper.UserMapper;
import org.max2ba.usernotificationservcei.proto.UserOperation;
import org.max2ba.usernotificationservcei.repository.UserRepository;
import org.max2ba.usernotificationservcei.service.UserService;
import org.max2ba.usernotificationservcei.specification.UserSpecification;
import org.max2ba.userservice.openapi.model.UserDto;
import org.max2ba.userservice.openapi.model.UserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserNotificationClient userNotificationClient;

    @Override
    public UserDto createUser(UserRequest request) {
        var saved = userRepository.save(userMapper.toUser(request));

        var future = userNotificationClient.notify(UserOperation.CREATE, saved.getEmail());

        future.thenAccept(resp ->
                log.info("Уведомление о создании отправлено: message={}, details={}",
                        resp.getMessage(), resp.getDetails())
        ).exceptionally(ex -> {
            log.error("Не удалось отправить уведомление о создании для {}", saved.getEmail(), ex);
            return null;
        });

        return userMapper.toUserDto(saved);
    }

    @Override
    @Transactional
    public UserDto updateUser(UUID id, UserRequest request) {
        var user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_EXCEPTION));
        userMapper.updateUserFromRequest(request, user);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(UUID id) {
        return userMapper.toUserDto(userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_EXCEPTION)));
    }

    @Override
    public Page<UserDto> searchUsers(String name, int page, int size) {
        return userRepository.findAll(UserSpecification.byFilters(name), PageRequest.of(page, size))
                .map(userMapper::toUserDto);
    }

    @Override
    public UserDto deleteUser(UUID id) {
        var user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_EXCEPTION));
        userRepository.delete(user);

        userNotificationClient.notify(UserOperation.DELETE, user.getEmail())
                .thenAccept(resp -> log.info("Уведомление об удалении отправлено: {}", resp.getMessage()))
                .exceptionally(ex -> {
                    log.error("Не удалось отправить уведомление об удалении для {}", user.getEmail(), ex);
                    return null;
                });

        return userMapper.toUserDto(user);
    }
}
