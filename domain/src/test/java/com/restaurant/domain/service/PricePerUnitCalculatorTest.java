package com.restaurant.domain.service;

import com.restaurant.domain.model.vo.Money;
import com.restaurant.domain.model.vo.Weight;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PricePerUnitCalculatorTest {

    @Test
    void testExactDivision() {
        // 100 pesos / 50 grams = 2.000000
        BigDecimal result = PricePerUnitCalculator.calculateCostPerGram(Money.ofPesos(100), Weight.ofGrams(50));
        assertThat(result).isEqualByComparingTo(new BigDecimal("2.000000"));
    }

    @Test
    void testRepeatingDecimals() {
        // 10 pesos / 3 grams = 3.333333
        BigDecimal result = PricePerUnitCalculator.calculateCostPerGram(Money.ofPesos(10), Weight.ofGrams(3));
        assertThat(result).isEqualByComparingTo(new BigDecimal("3.333333"));
    }

    @Test
    void testLargeNumbers() {
        // 500,000 pesos / 2,500,000 grams = 0.200000
        BigDecimal result = PricePerUnitCalculator.calculateCostPerGram(Money.ofPesos(500000), Weight.ofGrams(2500000));
        assertThat(result).isEqualByComparingTo(new BigDecimal("0.200000"));
    }

    @Test
    void testTinyQuantities() {
        // 5 pesos / 0.123 grams = 40.6504065... -> 40.650407
        BigDecimal result = PricePerUnitCalculator.calculateCostPerGram(Money.ofPesos(5), Weight.ofGrams(0.123));
        assertThat(result).isEqualByComparingTo(new BigDecimal("40.650407"));
    }

    @Test
    void testDivisionByZeroProtection() {
        assertThatThrownBy(() -> PricePerUnitCalculator.calculateCostPerGram(Money.ofPesos(10), Weight.ofGrams(0)))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Cannot divide by zero quantity");
    }
}
