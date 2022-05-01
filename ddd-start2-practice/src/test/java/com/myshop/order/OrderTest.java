package com.myshop.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.OrderLine;
import com.myshop.order.domain.orderline.OrderLines;
import com.myshop.order.domain.orderline.Quantity;
import com.myshop.order.domain.orderline.product.Product;
import com.myshop.order.domain.orderline.product.ProductId;
import com.myshop.order.domain.orderline.product.ProductName;
import com.myshop.order.domain.shipinfo.ReceiverAddress;
import com.myshop.order.domain.shipinfo.ReceiverName;
import com.myshop.order.domain.shipinfo.ReceiverTel;
import com.myshop.order.domain.shipinfo.ShipInfo;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderTest {

    private Order order;
    private OrderLines orderLines;
    private ShipInfo shipInfo;

    @BeforeEach
    void set() {
        this.orderLines = new OrderLines(
                Arrays.asList(
                        new OrderLine(
                                new Product(new ProductId("PRO_ID_1"), new ProductName("PR_NM_1"), Price.of(1000)),
                                Quantity.of(2)),
                        new OrderLine(new Product(new ProductId("PRO_ID_2"), new ProductName("PR_NM_2"), Price.of(700)),
                                Quantity.of(3)),
                        new OrderLine(new Product(new ProductId("PRO_ID_3"), new ProductName("PR_NM_3"), Price.of(500)),
                                Quantity.of(3))
                )
        );

        this.shipInfo = new ShipInfo(new ReceiverName("leevbongho"), new ReceiverAddress("신길로45길 7"), new ReceiverTel("010-5566-5081"));
        this.order = new Order(shipInfo, orderLines);
    }

    @Test
    void 생성자_파라미터_null일시_예외_발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Order(shipInfo, null);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(null, orderLines);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Order(null, null);
        });
    }


    @Test
    void 주문_항목_전쳬_금액_합_계산_테스트() {
        Price price = order.totalPrice();
        Price validationPrice = Price.of((1000*2) + (700*3) + (500*3));

        assertThat(price).isEqualTo(validationPrice);
    }

    @Test
    void 가격_지불_전_주문_상태_NOT_PAYED_여부_테스트() {
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.NOT_PAYED);
    }

    @Test
    void 주문_가격보다_적은_금액_지불시_예외_발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            order.pay(Price.of(1));
        });
    }

    @Test
    void 주문_가격_이상_금액_지불시_주문상태_PAYED_테스트() {
        payMoreThanAmount();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAYED);
    }


    @Test
    void 출고_완료_전_주문취소_시_주문상태_CANCELED_테스트() {
        order.cancel();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);

        payMoreThanAmount();
        order.cancel();
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
    }


    @Test
    void 출고_완료_후_주문취소_시_예외발생_테스트() {
        payMoreThanAmount();
        order.shipping();

        assertThrows(IllegalStateException.class, () -> {
            order.cancel();
        });
    }

    @Test
    void 출고_완료_전_배송지변경_가능여부_테스트() {
        payMoreThanAmount();
        ShipInfo changeShipInfo = new ShipInfo(
                new ReceiverName("leebong2"),
                new ReceiverAddress("우리집"),
                new ReceiverTel("010-5111-1111")
        );

        order.shipTo(changeShipInfo);

        assertThat(order.getShipInfo()).isEqualTo(changeShipInfo);
    }


    @Test
    void 출고_완료_후_배송지변경시_예외발생_테스트() {
        payMoreThanAmount();
        order.shipping();
        ShipInfo changeShipInfo = new ShipInfo(
                new ReceiverName("leebong2"),
                new ReceiverAddress("우리집"),
                new ReceiverTel("010-5111-1111")
        );

        assertThrows(IllegalStateException.class, () -> {
            order.shipTo(changeShipInfo);
        });

    }

    private void payMoreThanAmount() {
        order.pay(Price.of(order.totalPrice().getAmount() + 1));
    }
}
