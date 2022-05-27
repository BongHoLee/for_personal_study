package com.myshop.order;

import com.myshop.order.domain.Price;
import com.myshop.order.domain.orderline.OrderLines;
import com.myshop.order.domain.shipinfo.ShipInfo;

public class Order {
    private ShipInfo shipInfo;
    private OrderLines orderLines;
    private OrderStatus orderStatus;
    private Price totalPrice;

    public Order(ShipInfo shipInfo, OrderLines orderLines) {
        validationCheck(shipInfo, orderLines);
        setValues(shipInfo, orderLines);
    }

    private void setValues(ShipInfo shipInfo, OrderLines orderLines) {
        this.shipInfo = shipInfo;
        this.orderLines = orderLines;
        this.orderStatus = OrderStatus.NOT_PAYED;
        this.totalPrice = orderLines.totalPrice();
    }

    private void validationCheck(ShipInfo shipInfo, OrderLines orderLines) {
        if (shipInfo == null || orderLines == null) {
            throw new IllegalArgumentException("ORDER CONSTRUCTOR PARAMETER CANNOT BE NULL");
        }
    }

    public OrderStatus getOrderStatus() {
        return OrderStatus.valueOf(orderStatus.name());
    }

    public Price totalPrice() {
        return Price.of(totalPrice.getAmount());
    }

    public void pay(Price payedPrice) {
        if (totalPrice.compareWith(payedPrice) > 0) {
            throw new IllegalArgumentException("Payed Price(" + payedPrice + ") is less than ordered total price("+totalPrice+")");
        }
        changeStatusTo(OrderStatus.PAYED);
    }

    public void shipping() {
        changeStatusTo(OrderStatus.SHIPPING);
    }

    public void cancel() {
        changeStatusTo(OrderStatus.CANCELED);
    }

    private void changeStatusTo(OrderStatus status) {
        if (!orderStatus.canChangeTo(status)) {
            throw new IllegalStateException("CANNOT CHANGE ORDER STATUS FROM " + orderStatus + " TO " + status);
        }
        orderStatus = status;
    }

    public void shipTo(ShipInfo shipInfo) {
        if (orderStatus.isAfterShipping()) {
            throw new IllegalStateException("CANNOT CHANGE SHIP INFO WHEN AFTER SHIPPING");
        }

        this.shipInfo = shipInfo;
    }

    public ShipInfo getShipInfo() {
        return shipInfo;
    }
}
