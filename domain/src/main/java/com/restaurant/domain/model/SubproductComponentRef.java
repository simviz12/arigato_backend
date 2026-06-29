package com.restaurant.domain.model;

import lombok.Value;

import java.util.UUID;

@Value
public class SubproductComponentRef implements ComponentReference {
    UUID subproductId;

    public SubproductComponentRef(UUID subproductId) {
        if (subproductId == null) throw new IllegalArgumentException("Subproduct ID cannot be null");
        this.subproductId = subproductId;
    }

    @Override
    public UUID getReferenceId() {
        return subproductId;
    }
}
