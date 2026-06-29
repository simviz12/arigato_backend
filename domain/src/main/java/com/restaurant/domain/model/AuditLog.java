package com.restaurant.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    private UUID id;
    private UUID userId;
    private String actionType;
    private String entityId;
    
    @Column(columnDefinition = "jsonb")
    private String snapshot; // JSON string
    private LocalDateTime createdAt;
}
