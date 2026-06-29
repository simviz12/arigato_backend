package com.restaurant.domain.model.vo;

import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;

@EqualsAndHashCode
public class Weight {
    private final BigDecimal grams;

    public Weight(BigDecimal grams) {
        if (grams == null) {
            throw new IllegalArgumentException("Weight cannot be null");
        }
        if (grams.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Weight cannot be negative");
        }
        this.grams = grams;
    }

    public static Weight ofGrams(double grams) {
        return new Weight(BigDecimal.valueOf(grams));
    }

    public static Weight ofGrams(BigDecimal grams) {
        return new Weight(grams);
    }

    public static Weight ofKilograms(double kilograms) {
        return new Weight(BigDecimal.valueOf(kilograms).multiply(BigDecimal.valueOf(1000)));
    }

    public Weight add(Weight other) {
        return new Weight(this.grams.add(other.grams));
    }

    public Weight subtract(Weight other) {
        return new Weight(this.grams.subtract(other.grams));
    }

    public BigDecimal getGrams() {
        return grams;
    }

    public BigDecimal getKilograms() {
        return grams.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return grams.setScale(2, RoundingMode.HALF_UP).toPlainString() + "g";
    }
}
