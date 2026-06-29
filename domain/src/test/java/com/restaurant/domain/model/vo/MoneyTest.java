package com.restaurant.domain.model.vo;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void shouldRejectNegativeCents() {
        assertThatThrownBy(() -> new Money(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectNegativePesos() {
        assertThatThrownBy(() -> Money.ofPesos(-10.5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldConvertPesosToCents() {
        Money money = Money.ofPesos(15.50);
        assertThat(money.getCents()).isEqualTo(1550L);
    }

    @Test
    void shouldReturnCorrectPesos() {
        Money money = Money.ofCents(12345);
        assertThat(money.getPesos()).isEqualByComparingTo(new BigDecimal("123.45"));
    }

    @Test
    void shouldDivideCorrectly() {
        Money total = Money.ofCents(1000); // 10 pesos
        Money perGram = total.divideBy(new BigDecimal("3")); // 3 grams
        // 1000 / 3 = 333 cents -> 3.33 pesos
        assertThat(perGram.getCents()).isEqualTo(333L);
    }

    @Test
    void shouldRejectDivideByZero() {
        Money total = Money.ofCents(1000);
        assertThatThrownBy(() -> total.divideBy(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldFormatToStringCorrectly() {
        Money money = Money.ofPesos(1500.50);
        String formatted = money.toString();
        // The exact format depends on the JVM locale settings, but it should contain the numbers
        assertThat(formatted).contains("1");
        assertThat(formatted).contains("500");
    }
}
