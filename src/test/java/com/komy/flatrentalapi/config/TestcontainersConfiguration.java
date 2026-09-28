package com.komy.flatrentalapi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Shared Postgres container for every {@code @SpringBootTest}. Defining it as a bean
 * (rather than a {@code @Container}/{@code @Testcontainers} field on each test class) lets
 * Spring's context cache reuse the same container across test classes instead of starting
 * one per class.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:18-alpine");
    }
}
