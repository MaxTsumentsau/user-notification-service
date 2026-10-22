package org.max2ba.usernotificationservcei.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.max2ba.usernotificationservcei.constants.TestConstants;
import org.max2ba.usernotificationservcei.model.User;
import org.max2ba.usernotificationservcei.repository.UserRepository;
import org.max2ba.usernotificationservcei.utils.RandomClassGenerator;
import org.max2ba.usernotificationservcei.utils.TestContainersHelper;
import org.max2ba.userservice.openapi.model.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgres = TestContainersHelper.createPostgresContainer();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        TestContainersHelper.configurePostgresProperties(postgres, registry);
    }

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_shouldReturnCreatedUserAndSaveToDatabase() throws Exception {
        var request = RandomClassGenerator.create(UserRequest.class);
        request.setEmail("name@mail.ru");
        request.setAge(30);

        mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(request.getName()))
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.age").value(request.getAge()));

        assertThat(userRepository.count()).isEqualTo(1);
        var savedUser = userRepository.findAll().get(0);
        assertThat(savedUser.getName()).isEqualTo(request.getName());
        assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());
        assertThat(savedUser.getAge()).isEqualTo(request.getAge());
    }

    @Test
    void createUser_whenInvalidRequest_shouldReturnBadRequest() throws Exception {
        var request = new UserRequest();
        request.setName("");
        request.setEmail("invalid-email");

        mockMvc.perform(MockMvcRequestBuilders.post(TestConstants.BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.count()).isZero();
    }

    @Test
    void get_whenUserExists_shouldReturnUser() throws Exception {
        var user = RandomClassGenerator.create(User.class);
        user.setId(null);
        user.setEmail("get.user@example.com");
        user.setAge(30);

        var savedUser = userRepository.saveAndFlush(user);

        mockMvc.perform(MockMvcRequestBuilders.get(TestConstants.BASE_URL + "/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.age").value(user.getAge()));
    }

    @Test
    void get_whenUserNotFound_shouldThrowEntityNotFoundException() {
        var nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() ->
                mockMvc.perform(MockMvcRequestBuilders.get(TestConstants.BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void update_whenUserExists_shouldUpdateAndReturnUser() throws Exception {
        var user = RandomClassGenerator.create(User.class);
        user.setId(null);
        user.setEmail("initial@example.com");
        user.setName("Initial Name");
        user.setAge(25);

        var savedUser = userRepository.saveAndFlush(user);

        var updateRequest = new UserRequest().toBuilder()
                .name("Updated Name")
                .email("updated@example.com")
                .age(35)
                .build();

        mockMvc.perform(MockMvcRequestBuilders.put(TestConstants.BASE_URL + "/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value(updateRequest.getName()))
                .andExpect(jsonPath("$.email").value(updateRequest.getEmail()))
                .andExpect(jsonPath("$.age").value(updateRequest.getAge()));

        var updatedUserInDb = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUserInDb.getName()).isEqualTo(updateRequest.getName());
        assertThat(updatedUserInDb.getEmail()).isEqualTo(updateRequest.getEmail());
        assertThat(updatedUserInDb.getAge()).isEqualTo(updateRequest.getAge());
    }

    @Test
    void update_whenUserNotFound_shouldThrowEntityNotFoundException() {
        var nonExistentId = UUID.randomUUID();
        var updateRequest = new UserRequest().toBuilder()
                .name("Updated Name")
                .email("updated@example.com")
                .age(35)
                .build();

        assertThatThrownBy(() ->
                mockMvc.perform(MockMvcRequestBuilders.put(TestConstants.BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void update_whenInvalidRequest_shouldReturnBadRequest() throws Exception {
        var user = RandomClassGenerator.create(User.class);
        user.setId(null);
        user.setEmail("valid@example.com");
        user.setAge(25);

        var savedUser = userRepository.saveAndFlush(user);

        var invalidRequest = new UserRequest();
        invalidRequest.setName("");
        invalidRequest.setEmail("not-an-email");

        mockMvc.perform(MockMvcRequestBuilders.put(TestConstants.BASE_URL + "/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_whenUserExists_shouldDeleteAndReturnUser() throws Exception {
        var user = RandomClassGenerator.create(User.class);
        user.setId(null);
        user.setEmail("delete.user@example.com");
        user.setAge(40);

        var savedUser = userRepository.saveAndFlush(user);

        mockMvc.perform(MockMvcRequestBuilders.delete(TestConstants.BASE_URL + "/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.age").value(user.getAge()));

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    void delete_whenUserNotFound_shouldThrowEntityNotFoundException() {
        var nonExistentId = UUID.randomUUID();

        assertThatThrownBy(() ->
                mockMvc.perform(MockMvcRequestBuilders.delete(TestConstants.BASE_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
        ).hasCauseInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void search_whenMatchingNameExists_shouldReturnPagedUsers() throws Exception {
        var user1 = User.builder().name("Ivan Petrov").email("ivan@example.com").age(25).build();
        var user2 = User.builder().name("Anna Ivanova").email("anna@example.com").age(28).build();
        var user3 = User.builder().name("John Doe").email("john@example.com").age(35).build();

        userRepository.saveAllAndFlush(List.of(user1, user2, user3));

        mockMvc.perform(MockMvcRequestBuilders.get(TestConstants.BASE_URL + "/search")
                        .param("name", "ivan")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void search_whenNoFilters_shouldReturnAllUsersPaged() throws Exception {
        var user1 = User.builder().name("Ivan Petrov").email("ivan@example.com").age(25).build();
        var user2 = User.builder().name("John Doe").email("john@example.com").age(35).build();

        userRepository.saveAllAndFlush(List.of(user1, user2));

        mockMvc.perform(MockMvcRequestBuilders.get(TestConstants.BASE_URL + "/search")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
