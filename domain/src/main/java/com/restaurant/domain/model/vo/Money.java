package com.restaurant.domain.model.vo;

import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

@EqualsAndHashCode
public class Money {
    private final long cents;

    public Money(long cents) {
        if (cents < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        this.cents = cents;
    }

    public static Money ofCents(long cents) {
        return new Money(cents);
    }

    public static Money ofPesos(double pesos) {
        if (pesos < 0) {
            throw new IllegalArgumentException("Money cannot be negative");
        }
        return new Money(Math.round(pesos * 100));
    }

    public Money add(Money other) {
        return new Money(this.cents + other.cents);
    }

    public long getCents() {
        return cents;
    }
    
    public BigDecimal getPesos() {
        return BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Money divideBy(BigDecimal divisor) {
        if (divisor == null || divisor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Divisor must be greater than zero");
        }
        BigDecimal resultCents = BigDecimal.valueOf(cents).divide(divisor, 0, RoundingMode.HALF_UP);
        return new Money(resultCents.longValue());
    }

    @Override
    public String toString() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        return format.format(getPesos());
    }
}
