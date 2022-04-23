package com.myshop.order.domain.orderline;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.product.Product;

public class OrderLine {
    private final Product product;
    private final Quantity quantity;

    public OrderLine(Product product, Quantity quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Price totalPrice() {
        return Price.of(product.getPrice().getAmount() * quantity.getValue());
    }
}
