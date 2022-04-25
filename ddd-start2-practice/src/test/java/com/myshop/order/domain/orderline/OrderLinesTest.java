package com.myshop.order.domain.orderline;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.product.Product;
import com.myshop.order.domain.orderline.product.ProductId;
import com.myshop.order.domain.orderline.product.ProductName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class OrderLinesTest {

    @Test
    void 주문항목_1개_미만일경우_예외발생_테스트() {
        List<OrderLine> orderLines = new ArrayList<>();
        assertThrows(IllegalArgumentException.class, () -> {
            new OrderLines(orderLines);
        });
    }

    @Test
    void 주문항목_null일경우_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new OrderLines(null);
        });
    }

    @Test
    void 주문항목_총가격_합산_계산_테스트() {

        List<OrderLine> orderLines = Arrays.asList(
                new OrderLine(new Product(new ProductId("PRO1"), new ProductName("PRNM1"), Price.of(1000)),
                        Quantity.of(3)),
                new OrderLine(new Product(new ProductId("PRO2"), new ProductName("PRNM2"), Price.of(500)),
                        Quantity.of(2)),
                new OrderLine(new Product(new ProductId("PRO3"), new ProductName("PRNM3"), Price.of(700)),
                        Quantity.of(3)));

        long result = (1000*3) + (500*2) + (700*3);
        OrderLines orders = new OrderLines(orderLines);

        assertThat(result).isEqualTo(orders.totalPrice().getAmount());
    }

}
