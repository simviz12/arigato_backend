package com.restaurant.application.audit;

import java.util.UUID;

public record AuditTrailEvent(
        UUID userId,
        String actionType,
        String entityId,
        String snapshot
) {}
