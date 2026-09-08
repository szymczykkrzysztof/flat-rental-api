package com.komy.flatrentalapi.repository;

import com.komy.flatrentalapi.entity.User;
import com.komy.flatrentalapi.entity.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Testcontainers
public class UserRepositoryTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldSaveAndRetrieveUser() {
        var user = new User();
        user.setEmail("email@test.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPasswordHash("password");
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();
        var userFromDb = userRepository.findById(user.getId()).orElseThrow();
        assertThat(userFromDb).isEqualTo(user);
        assertThat(userFromDb.getEmail()).isEqualTo("email@test.com");
        assertThat(userFromDb.getId()).isNotNull();
        assertThat(userFromDb.getId()).isEqualTo(user.getId());
        assertThat(userFromDb.getFirstName()).isEqualTo("John");
        assertThat(userFromDb.getLastName()).isEqualTo("Doe");
        assertThat(user.getPasswordHash()).isEqualTo(userFromDb.getPasswordHash());
        assertThat(user.getRole()).isEqualTo(userFromDb.getRole());
    }
}
