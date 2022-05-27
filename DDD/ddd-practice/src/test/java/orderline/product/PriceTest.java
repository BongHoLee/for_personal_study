package orderline.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PriceTest {

    @Test
    void 생성_값_정상_반환_테스트() {
        long value = 1000;
        Price price = Price.of(value);
        assertThat(price.value()).isEqualTo(value);
    }

    @Test
    void 음수값_전달시_예외반환_테스트() {
        long value = -1;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Price price = Price.of(value);
        });
    }

    @Test
    void 가격_덧셈_테스트() {
        Price priceA = Price.of(1000);
        Price priceB = Price.of(2000);
        Price price = priceA.sumWith(priceB);

        assertThat(price.value()).isEqualTo(1000 + 2000);
    }

}
