package com.restaurant.application.audit;

import com.restaurant.domain.model.AuditLog;
import com.restaurant.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditTrailEvent(AuditTrailEvent event) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .userId(event.userId())
                    .actionType(event.actionType())
                    .entityId(event.entityId())
                    .snapshot(event.snapshot())
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Log it but do not throw, as we don't want to crash the async thread or affect main tx (already committed anyway if using TransactionalEventListener but here we just catch)
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
        }
    }
}
