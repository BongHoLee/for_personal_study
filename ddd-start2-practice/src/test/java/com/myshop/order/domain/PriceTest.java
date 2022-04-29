package com.myshop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class PriceTest {

    @Test
    void 생성시_전달값_반환_테스트() {
        long value = 100;
        Price price = Price.of(value);
        assertThat(price).isEqualTo(Price.of(value));
        assertThat(price.hashCode()).isEqualTo(Price.of(value).hashCode());
    }

    @Test
    void 가격_합산_계산_테스트() {
        Price originPrice = Price.of(100);
        Price targetPrice = Price.of(200);

        Price price = originPrice.sumWith(targetPrice);

        assertThat(price).isEqualTo(Price.of(300));
    }

    @Test
    void 음수_가격_전달시_예외_발생_테스트() {
        long minusValue = -100;
        assertThrows(IllegalArgumentException.class, () -> {
            Price.of(minusValue);
        });
    }

    @Test
    void 비교_가격_더_작은_경우_테스트() {
        Price origin = Price.of(100);
        Price target = Price.of(200);

        assertThat(origin.compareWith(target)).isEqualTo(-100);
    }

    @Test
    void 비교_가격_더_큰_경우_테스트() {
        Price origin = Price.of(200);
        Price target = Price.of(100);

        assertThat(origin.compareWith(target)).isEqualTo(100);
    }
}
