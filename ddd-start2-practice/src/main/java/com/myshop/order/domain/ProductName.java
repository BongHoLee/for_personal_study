package com.myshop.order.domain;

import java.util.Objects;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class ProductName {
    private final String productName;

    public ProductName(String productName) {
        validateProductName(productName);
        this.productName = productName;
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException("PRODUCT NAME CANNOT BE EMPTY");
        }
    }

    public String getProductName() {
        return productName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductName that = (ProductName) o;
        return getProductName().equals(that.getProductName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProductName());
    }
}
