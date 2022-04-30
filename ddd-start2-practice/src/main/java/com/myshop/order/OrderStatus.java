package com.myshop.order;

import java.util.function.Function;

public enum OrderStatus {
    NOT_PAYED(target -> false),
    PAYED(target -> target.equals(OrderStatus.NOT_PAYED)),
    SHIPPING(target -> target.equals(OrderStatus.PAYED)),
    DELIVERY_COMPLETED(target -> target.equals(OrderStatus.SHIPPING)),
    CANCELED(target -> target.equals(OrderStatus.NOT_PAYED) || target.equals(OrderStatus.PAYED));

    private Function<OrderStatus, Boolean> function;

    OrderStatus(Function<OrderStatus, Boolean> function) {
        this.function = function;
    }

    public boolean canChangeTo(OrderStatus target) {
        return function.apply(target);
    }
}
