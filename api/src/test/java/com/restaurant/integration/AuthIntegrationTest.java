package com.restaurant.integration;

import com.restaurant.domain.model.Role;
import com.restaurant.domain.model.User;
import com.restaurant.domain.repository.UserRepository;
import com.restaurant.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class AuthIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api";
        // Create test user if not exists
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .id(UUID.randomUUID())
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("password"))
                    .role(Role.ADMIN)
                    .fullName("Admin User")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
        }
        if (userRepository.findByUsername("cashier").isEmpty()) {
            User cashier = User.builder()
                    .id(UUID.randomUUID())
                    .username("cashier")
                    .passwordHash(passwordEncoder.encode("password"))
                    .role(Role.CASHIER)
                    .fullName("Cashier User")
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            userRepository.save(cashier);
        }
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("password");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.SET_COOKIE)).isNotEmpty();
        assertThat(response.getBody()).contains("accessToken");
    }

    @Test
    void testLoginFailure() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/auth/login", request, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); // or 401 depending on exception handler
    }

    @Test
    void testAdminEndpointAccess() {
        // Login as cashier
        LoginRequest req = new LoginRequest();
        req.setUsername("cashier");
        req.setPassword("password");
        ResponseEntity<String> loginRes = restTemplate.postForEntity(baseUrl + "/auth/login", req, String.class);
        
        // Extract token manually from JSON
        String body = loginRes.getBody();
        String token = body.split("\"accessToken\":\"")[1].split("\"")[0];

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(baseUrl + "/admin/test", HttpMethod.GET, entity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN); // 403 Cashier blocked
    }
}
