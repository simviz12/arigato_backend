package com.restaurant.domain.model;

import java.util.UUID;

/**
 * Algebraic Data Type (ADT) / Sealed Interface to ensure a component references
 * EXACTLY one type of product (Primary OR Subproduct).
 */
public sealed interface ComponentReference permits PrimaryComponentRef, SubproductComponentRef {
    UUID getReferenceId();
}
