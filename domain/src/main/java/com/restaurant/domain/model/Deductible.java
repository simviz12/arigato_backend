package com.restaurant.domain.model;

import java.math.BigDecimal;

public interface Deductible {
    void deduct(BigDecimal amount);
}
