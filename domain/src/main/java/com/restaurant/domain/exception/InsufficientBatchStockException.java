package com.restaurant.domain.exception;

public class InsufficientBatchStockException extends RuntimeException {
    public InsufficientBatchStockException(String message) {
        super(message);
    }
}
