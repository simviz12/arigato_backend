package com.restaurant.infrastructure;

import com.restaurant.domain.model.Role;
import com.restaurant.infrastructure.entity.UserEntity;
import com.restaurant.infrastructure.repository.SpringDataUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class RepositorySmokeTest {

    @Autowired
    private SpringDataUserRepository userRepository;

    @Test
    public void testSaveAndFindUser() {
        UUID id = UUID.randomUUID();
        UserEntity user = UserEntity.builder()
                .id(id)
                .username("admin_test")
                .passwordHash("hash")
                .role(Role.ADMIN)
                .fullName("Admin Test")
                .active(true)
                .build();

        userRepository.save(user);

        UserEntity found = userRepository.findById(id).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getUsername()).isEqualTo("admin_test");
    }

    // A dummy configuration class is needed if we don't have a SpringBootApplication in this module
    @SpringBootApplication
    static class TestConfig {
    }
}
