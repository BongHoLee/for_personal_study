package order;

import orderline.OrderLine;
import shipInfo.ShipInfo;

public class Order {
    private ShipInfo shipInfo;
    private OrderLine orderLine;
    private OrderState orderState;


    public Order(ShipInfo shipInfo, OrderLine orderLine) {
        this.shipInfo = shipInfo;
        this.orderLine = orderLine;
        this.orderState = OrderState.READY;
    }

    public void changeShipInfo(ShipInfo changedShipInfo) {
        if (orderState.canNotChangeOrderContents()) {
            throw new IllegalStateException(orderState.name() + " can not change ship info");
        }

        this.shipInfo = changedShipInfo;
    }

    public ShipInfo getShipInfo() {
        return this.shipInfo;
    }

    public void release() {
        this.orderState = OrderState.RELEASE;
    }

    public void cancel() {
        if (orderState.canNotChangeOrderContents()) {
            throw new IllegalStateException(orderState.name() + " can not cancel order");
        }
        this.orderState = OrderState.CANCELED;
    }
}
