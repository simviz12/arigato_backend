package com.restaurant.domain.repository;

import com.restaurant.domain.model.AuditLog;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends CrudRepository<AuditLog, UUID> {
}
