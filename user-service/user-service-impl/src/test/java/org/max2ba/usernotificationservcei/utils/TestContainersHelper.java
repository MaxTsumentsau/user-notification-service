package org.max2ba.usernotificationservcei.utils;

import lombok.experimental.UtilityClass;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

@UtilityClass
public class TestContainersHelper {

    public static PostgreSQLContainer<?> createPostgresContainer() {
        return new PostgreSQLContainer<>("postgres:16-alpine")
                .withDatabaseName("testdb_" + UUID.randomUUID())
                .withUsername("test")
                .withPassword("test")
                .withReuse(false);
    }

    public static void configurePostgresProperties(PostgreSQLContainer<?> postgres,
                                                   DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
