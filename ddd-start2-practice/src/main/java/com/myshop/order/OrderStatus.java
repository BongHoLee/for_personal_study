package com.myshop.order;

import java.util.function.Function;

public enum OrderStatus {
    CANCELED(-1, target -> false),
    DELIVERY_COMPLETED(3, target -> false),
    SHIPPING(2, target -> target.equals(OrderStatus.DELIVERY_COMPLETED)),
    PAYED(1, target -> target.equals(OrderStatus.SHIPPING) || target.equals(OrderStatus.CANCELED)),
    NOT_PAYED(0, target -> target.equals(OrderStatus.PAYED) || target.equals(OrderStatus.CANCELED));

    private Function<OrderStatus, Boolean> function;
    private int sequence;

    OrderStatus(int sequence, Function<OrderStatus, Boolean> function) {
        this.sequence = sequence;
        this.function = function;
    }

    public boolean canChangeTo(OrderStatus target) {
        return function.apply(target);
    }

    public boolean isAfterShipping() {
        return this.sequence >= SHIPPING.sequence;
    }

}
