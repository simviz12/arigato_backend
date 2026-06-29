package com.restaurant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private Role role;
    private String fullName;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
