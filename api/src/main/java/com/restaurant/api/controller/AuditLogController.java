package com.restaurant.api.controller;

import com.restaurant.domain.model.AuditLog;
import com.restaurant.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-log")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        List<AuditLog> logs = new ArrayList<>();
        auditLogRepository.findAll().forEach(logs::add);
        return ResponseEntity.ok(logs);
    }
}
