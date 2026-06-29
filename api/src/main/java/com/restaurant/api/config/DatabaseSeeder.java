package com.restaurant.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (count == null || count == 0) {
            String adminPassword = passwordEncoder.encode("admin123");
            jdbcTemplate.update(
                    "INSERT INTO users (id, username, password_hash, role, full_name, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(), "admin@arigato.com", adminPassword, "ADMIN", "Administrador Principal", true, LocalDateTime.now(), LocalDateTime.now()
            );

            String cashierPassword = passwordEncoder.encode("cajero123");
            jdbcTemplate.update(
                    "INSERT INTO users (id, username, password_hash, role, full_name, active, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                    UUID.randomUUID(), "cajero@arigato.com", cashierPassword, "CASHIER", "Cajero Turno 1", true, LocalDateTime.now(), LocalDateTime.now()
            );

            System.out.println("===========================================");
            System.out.println("Cuentas por defecto creadas:");
            System.out.println("ADMIN: admin@arigato.com / admin123");
            System.out.println("CAJERO: cajero@arigato.com / cajero123");
            System.out.println("===========================================");
        }
    }
}
