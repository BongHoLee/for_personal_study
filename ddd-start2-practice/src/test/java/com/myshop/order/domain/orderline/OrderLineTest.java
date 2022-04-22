package com.myshop.order.domain.orderline;

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
                )
        );
    }

    @Test
    void createTest() {

    }
}
