package com.restaurant.application.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
public class AuditLoggerAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public AuditLoggerAspect(ApplicationEventPublisher eventPublisher, ObjectMapper objectMapper) {
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(auditable)")
    public Object logAction(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        Object result = joinPoint.proceed(); // Let the target method execute first

        try {
            // Extract current User ID from Spring Security
            UUID userId = extractCurrentUserId();

            // Attempt to serialize arguments for snapshot
            String snapshot = null;
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                snapshot = objectMapper.writeValueAsString(args);
            }

            // Publish event to be processed asynchronously
            AuditTrailEvent event = new AuditTrailEvent(
                    userId,
                    auditable.action(),
                    null, // Could extract entityId if returned object has ID
                    snapshot
            );
            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            // We must NEVER block or fail the main business transaction just because auditing failed
            e.printStackTrace();
        }

        return result;
    }

    private UUID extractCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            try {
                // Assuming principal is the username or userId string. In this app, authUseCase uses username.
                // For MVP, we will use a deterministic UUID or if the principal is the UUID.
                // We'll parse it or return a dummy UUID for the admin if it's "admin".
                String name = auth.getName();
                return UUID.nameUUIDFromBytes(name.getBytes()); 
            } catch (Exception e) {
                return UUID.randomUUID();
            }
        }
        return UUID.fromString("00000000-0000-0000-0000-000000000000"); // SYSTEM
    }
}
