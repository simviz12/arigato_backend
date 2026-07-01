package com.restaurant.application.usecase;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;

public class MarginCalculationTest {

    @Test
    public void testHealthyMarginCalculation() {
        // Given a plate that costs 4500 to produce
        BigDecimal productionCost = new BigDecimal("4500");
        // And is sold for 12000
        BigDecimal sellingPrice = new BigDecimal("12000");

        // When
        BigDecimal marginAmount = sellingPrice.subtract(productionCost);
        BigDecimal marginPercentage = marginAmount.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal("100"));

        // Then
        assertEquals(new BigDecimal("7500"), marginAmount);
        assertEquals(new BigDecimal("62.5000"), marginPercentage); // 62.5% is very healthy
        assertTrue(marginPercentage.compareTo(new BigDecimal("30")) > 0);
    }

    @Test
    public void testDangerousMarginCalculation() {
        // Given a plate that costs 8000 to produce
        BigDecimal productionCost = new BigDecimal("8000");
        // And is sold for 10000
        BigDecimal sellingPrice = new BigDecimal("10000");

        // When
        BigDecimal marginAmount = sellingPrice.subtract(productionCost);
        BigDecimal marginPercentage = marginAmount.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal("100"));

        // Then
        assertEquals(new BigDecimal("2000"), marginAmount);
        assertEquals(new BigDecimal("20.0000"), marginPercentage); // 20% is below industry standard (30%)
        assertTrue(marginPercentage.compareTo(new BigDecimal("30")) < 0);
    }
    
    @Test
    public void testLossCalculation() {
        // Given a plate that costs 12000 to produce
        BigDecimal productionCost = new BigDecimal("12000");
        // And is sold for 10000
        BigDecimal sellingPrice = new BigDecimal("10000");

        // When
        BigDecimal marginAmount = sellingPrice.subtract(productionCost);
        BigDecimal marginPercentage = marginAmount.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal("100"));

        // Then
        assertEquals(new BigDecimal("-2000"), marginAmount);
        assertEquals(new BigDecimal("-20.0000"), marginPercentage); // -20% is a loss
        assertTrue(marginPercentage.compareTo(BigDecimal.ZERO) < 0);
    }
}
