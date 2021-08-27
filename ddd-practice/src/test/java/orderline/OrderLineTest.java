package orderline;

import java.util.Arrays;
import orderline.product.Price;
import orderline.product.Product;
import orderline.product.Volume;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrderLineTest {

    @Test
    void createOrderLineTest() {
        new OrderLine(
                Arrays.asList(
                        new Product("pr1", "prn1", Volume.of(10), Price.of(2)),
                        new Product("pr2", "prn2", Volume.of(11), Price.of(3)),
                        new Product("pr3", "prn3", Volume.of(12), Price.of(4)),
                        new Product("pr4", "prn4", Volume.of(13), Price.of(5))
                )
        );
    }

    @Test
    void 가격_계산_태스트() {
        OrderLine orderLine = new OrderLine(
                Arrays.asList(
                        new Product("pr1", "prn1", Volume.of(10), Price.of(2)),
                        new Product("pr2", "prn2", Volume.of(11), Price.of(3))
                )
        );

        Assertions.assertThat(orderLine.price().value()).isEqualTo(10*2 + 11*3);
    }

}
