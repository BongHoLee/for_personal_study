package com.myshop.order.domain.orderline.product;

import java.util.Objects;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class ProductId {
    private final String productId;

    public ProductId(String productId) {
        validationId(productId);
        this.productId = productId;
    }

    private void validationId(String productId) {
        if (productId == null || productId.isEmpty())
            throw new IllegalArgumentException("PRODUCT ID CANNOT BE EMPTY");
    }

    public String getProductId() {
        return productId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductId productId1 = (ProductId) o;
        return getProductId().equals(productId1.getProductId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductId());
    }
}
