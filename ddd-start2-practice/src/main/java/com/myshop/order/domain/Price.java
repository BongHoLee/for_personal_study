package com.myshop.order.domain;

import java.util.Objects;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-05-20
 */

public class Price {
    private static final long MIN_VALUE = 0;
    private final long value;

    private Price(long value) {
        this.value = value;
    }

    public static Price of(long value) {
        checkMinValue(value);
        return new Price(value);
    }

    private static void checkMinValue(long value) {
        if (value < MIN_VALUE)
            throw new IllegalArgumentException("PRICE CANNOT BE LESS THAN 0");
    }

    public Price sumWith(Price targetPrice) {
        return Price.of(value + targetPrice.getAmount());
    }

    public long getAmount() {
        return this.value;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Price price = (Price) o;
        return value == price.value;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(value);
    }

}
