package com.myshop.order.domain.orderline;

import java.util.Objects;

public class Quantity {
    private static final long MIN_VALUE = 0;
    private final long value;

    private Quantity(long value) {
        this.value = value;
    }

    public static Quantity of(long value) {
        validationCheck(value);
        return new Quantity(value);
    }

    private static void validationCheck(long value) {
        if (value < MIN_VALUE)
            throw new IllegalArgumentException("QUANTITY CANNOT BE LESS THAN 0");
    }

    public long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Quantity quantity = (Quantity) o;
        return value == quantity.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public Quantity add(Quantity target) {
        return Quantity.of(this.getValue() + target.getValue());
    }
}
