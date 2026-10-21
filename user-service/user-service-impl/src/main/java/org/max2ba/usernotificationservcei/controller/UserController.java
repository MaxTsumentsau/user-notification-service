package org.max2ba.usernotificationservcei.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.max2ba.usernotificationservcei.service.UserService;
import org.max2ba.userservice.openapi.api.UsersApi;
import org.max2ba.userservice.openapi.model.UserDto;
import org.max2ba.userservice.openapi.model.UserRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController implements UsersApi {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody UserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public UserDto update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @GetMapping("/{id}")
    public UserDto get(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @GetMapping("/search")
    public Page<UserDto> search(@RequestParam(required = false) String name,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "5") int size) {
        return userService.searchUsers(name, page, size);
    }

    @DeleteMapping("/{id}")
    public UserDto delete(@PathVariable UUID id) {
        return userService.deleteUser(id);
    }
}
