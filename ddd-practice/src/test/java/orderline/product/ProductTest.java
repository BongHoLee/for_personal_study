package orderline.product;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ProductTest {

    @Test
    void 생성_테스트() {
        Product product = new Product(
                "productName",
                "productCode",
                Volume.of(10),
                Price.of(1000)
        );
    }

    @Test
    void 가격_반환_테스트() {
        long vol = 10;
        long price = 1000;

        Product product = new Product(
                "productName",
                "productCode",
                Volume.of(vol),
                Price.of(price)
        );

        Assertions.assertThat(product.price().value()).isEqualTo(vol * price);
    }
}
