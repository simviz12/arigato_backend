package com.restaurant.domain.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Distributor {
    private final UUID id;
    private String name;
    private String contactPhone;
    private String contactEmail;
    private String notes;
    private boolean active;
    private final LocalDateTime createdAt;

    @Builder
    public Distributor(UUID id, String name, String contactPhone, String contactEmail, String notes,
                       Boolean active, LocalDateTime createdAt) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Distributor name cannot be empty");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.name = name;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.notes = notes;
        this.active = active != null ? active : true;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateContactInfo(String phone, String email) {
        this.contactPhone = phone;
        this.contactEmail = email;
    }
}
