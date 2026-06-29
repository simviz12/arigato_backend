package com.restaurant.domain.model.vo;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightTest {

    @Test
    void shouldRejectNullWeight() {
        assertThatThrownBy(() -> new Weight(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativeWeight() {
        assertThatThrownBy(() -> new Weight(new BigDecimal("-1.0")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateZeroWeight() {
        Weight zero = new Weight(BigDecimal.ZERO);
        assertThat(zero.getGrams()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void shouldConvertGramsToKilograms() {
        Weight w = Weight.ofGrams(1500);
        assertThat(w.getKilograms()).isEqualByComparingTo(new BigDecimal("1.5"));
    }

    @Test
    void shouldConvertKilogramsToGrams() {
        Weight w = Weight.ofKilograms(2.5);
        assertThat(w.getGrams()).isEqualByComparingTo(new BigDecimal("2500"));
    }

    @Test
    void shouldAddAndSubtractWeights() {
        Weight w1 = Weight.ofGrams(100);
        Weight w2 = Weight.ofGrams(50);
        
        assertThat(w1.add(w2).getGrams()).isEqualByComparingTo(new BigDecimal("150"));
        assertThat(w1.subtract(w2).getGrams()).isEqualByComparingTo(new BigDecimal("50"));
    }

    @Test
    void shouldFormatToStringCorrectly() {
        Weight w = Weight.ofGrams(1250.5);
        assertThat(w.toString()).isEqualTo("1250.50g");
    }
}
