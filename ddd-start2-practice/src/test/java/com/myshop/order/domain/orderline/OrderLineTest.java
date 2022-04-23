package com.myshop.order.domain.orderline;

import static org.assertj.core.api.Assertions.assertThat;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.product.Product;
import com.myshop.order.domain.orderline.product.ProductId;
import com.myshop.order.domain.orderline.product.ProductName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OrderLineTest {

    private OrderLine orderLine;

    @BeforeEach
    void set() {
        this.orderLine = new OrderLine(
                new Product(
                        new ProductId("product_001"),
                        new ProductName("productName"),
                        Price.of(1000)
                ),
                Quantity.of(10)
        );
    }

    @Test
    void 가격_1000원_수량_10개_총가격_10000_반환_테스트() {
        Price price = orderLine.totalPrice();
        assertThat(price).isEqualTo(Price.of(10000));
    }
}
