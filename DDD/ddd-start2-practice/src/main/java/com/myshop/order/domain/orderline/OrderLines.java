package com.myshop.order.domain.orderline;

import com.myshop.order.domain.Price;
import java.util.List;
import java.util.stream.Collectors;

public class OrderLines {
    private final List<OrderLine> orderLines;

    public OrderLines(List<OrderLine> orderLines) {
        validationCheck(orderLines);
        this.orderLines = orderLines;
    }

    private void validationCheck(List<OrderLine> orderLines) {
        if (orderLines == null || orderLines.isEmpty()) {
            throw new IllegalArgumentException("ORDERLINES SIZE CANNOT BE LESS THAN 1");
        }
    }

    public Price totalPrice() {
        return Price.of(
                orderLines.stream()
                        .mapToLong(eachOrderLine -> eachOrderLine.totalPrice().getAmount())
                        .sum()
        );
    }
}
