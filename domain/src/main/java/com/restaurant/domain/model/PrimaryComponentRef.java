package com.restaurant.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class PrimaryComponentRef implements ComponentReference {
    UUID primaryProductId;

    public PrimaryComponentRef(UUID primaryProductId) {
        if (primaryProductId == null) throw new IllegalArgumentException("Primary product ID cannot be null");
        this.primaryProductId = primaryProductId;
    }

    @Override
    public UUID getReferenceId() {
        return primaryProductId;
    }
}
