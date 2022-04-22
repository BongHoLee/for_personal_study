package com.myshop.order.domain.orderline.product;

import com.myshop.order.domain.Price;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class Product {
    private final ProductId productId;
    private final ProductName productName;
    private final Price price;

    public Product(ProductId productId, ProductName productName, Price price) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
    }

}
