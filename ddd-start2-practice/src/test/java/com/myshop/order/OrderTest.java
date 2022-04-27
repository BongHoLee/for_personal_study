package com.myshop.order;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.OrderLine;
import com.myshop.order.domain.orderline.OrderLines;
import com.myshop.order.domain.orderline.Quantity;
import com.myshop.order.domain.orderline.product.Product;
import com.myshop.order.domain.orderline.product.ProductId;
import com.myshop.order.domain.orderline.product.ProductName;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class OrderTest {

    @Test
    void createTest() {
        OrderLines orderLines = new OrderLines(
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

        new Order(

        );
    }
}
