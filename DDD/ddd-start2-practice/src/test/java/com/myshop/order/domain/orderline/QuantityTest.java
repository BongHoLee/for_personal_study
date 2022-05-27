package com.myshop.order.domain.orderline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class QuantityTest {

    @Test
    void createTest() {
        long value = 2;
        Quantity quantity = Quantity.of(value);
    }

    @Test
    void 같은_value일시_equals_true_반환_테스트() {
        long value = 2;
        Quantity first = Quantity.of(value);
        Quantity second = Quantity.of(value);

        assertThat(first).isEqualTo(second);
    }

    @Test
    void 음수_value일시_예외발생_반환_테스트() {
        long value = -1;
        assertThrows(IllegalArgumentException.class, () -> {
            Quantity.of(value);
        });
    }

    @Test
    void zero_value일시_예외발생_반환_테스트() {
        long value = 0;
        assertThrows(IllegalArgumentException.class, () -> {
            Quantity.of(value);
        });
    }

    @Test
    void 값_합산_계산_테스트() {
        long firstValue = 1;
        long secondValue = 2;

        Quantity firstQuantity = Quantity.of(firstValue);
        Quantity secondQuantity = Quantity.of(secondValue);

        Quantity result = firstQuantity.add(secondQuantity);
        assertThat(result.getValue()).isEqualTo(firstValue + secondValue);
    }
}
