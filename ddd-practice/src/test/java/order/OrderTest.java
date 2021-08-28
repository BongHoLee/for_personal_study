package order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import orderline.OrderLine;
import orderline.product.Price;
import orderline.product.Product;
import orderline.product.Volume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import shipInfo.Address;
import shipInfo.CustomerName;
import shipInfo.ShipInfo;
import shipInfo.Tel;

public class OrderTest {

    Order order;
    @BeforeEach
    void setOrder() {
        ShipInfo shipInfo = new ShipInfo(new CustomerName("lee"), new Tel("010-5511-5081"), new Address("신길로 45길 7"));
        OrderLine orderLine = new OrderLine(Arrays.asList(
                new Product("prn1", "prc1", Volume.of(2), Price.of(100)),
                new Product("prn2", "prc2", Volume.of(2), Price.of(200)),
                new Product("prn3", "prc3", Volume.of(1), Price.of(500)),
                new Product("prn4", "prc4", Volume.of(3), Price.of(300))
        ));

        order = new Order(shipInfo, orderLine);
    }

    @Test
    void 출고전_배송지정보_수정_테스트() {
        String name = "lee";
        String tel = "010-5511-5082";
        String address = "신길로 42길";
        ShipInfo changedShipInfo = new ShipInfo(new CustomerName(name), new Tel(tel), new Address(address));

        assertDoesNotThrow(() -> {
            order.changeShipInfo(changedShipInfo);
        });

        assertThat(order.getShipInfo().getAddress().toString()).isEqualTo(address);
        assertThat(order.getShipInfo().getName().toString()).isEqualTo(name);
        assertThat(order.getShipInfo().getTel().toString()).isEqualTo(tel);
    }

    @Test
    void 출고후_배송지정보_수정시_예외발생() {

        order.release();
        String name = "kim";
        String tel = "010-5511-5082";
        String address = "신길로 42길";
        ShipInfo changedShipInfo = new ShipInfo(new CustomerName(name), new Tel(tel), new Address(address));

        assertThrows(IllegalStateException.class, () -> {
            order.changeShipInfo(changedShipInfo);
        });

        assertThat(order.getShipInfo().getAddress().toString()).isNotEqualTo(address);
        assertThat(order.getShipInfo().getName().toString()).isNotEqualTo(name);
        assertThat(order.getShipInfo().getTel().toString()).isNotEqualTo(tel);
    }

    @Test
    void 출고전_주문취소_가능() {
        assertDoesNotThrow(() -> {
            order.cancel();
        });
    }

    @Test
    void 출고후_주문취소_불가능() {
        order.release();
        assertThrows(IllegalStateException.class, () -> {
            order.cancel();
        });
    }
}
