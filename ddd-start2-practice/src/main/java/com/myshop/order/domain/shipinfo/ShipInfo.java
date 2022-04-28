package com.myshop.order.domain.shipinfo;

import java.util.Objects;

public class ShipInfo {
    private final ReceiverName receiverName;
    private final ReceiverAddress receiverAddress;

    public ShipInfo(ReceiverName receiverName, ReceiverAddress receiverAddress) {
        checkValidation(receiverName, receiverAddress);
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
    }

    private void checkValidation(ReceiverName receiverName, ReceiverAddress receiverAddress) {
        if (receiverName == null || receiverAddress == null) {
            throw new IllegalArgumentException("RECEIVER CANNOT HAS NULL NAME OR NULL ADDRESS");
        }
    }

    public ReceiverAddress getReceiverAddress() {
        return receiverAddress;
    }

    public ReceiverName getReceiverName() {
        return receiverName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShipInfo shipInfo = (ShipInfo) o;
        return getReceiverName().equals(shipInfo.getReceiverName()) && getReceiverAddress().equals(
                shipInfo.getReceiverAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReceiverName(), getReceiverAddress());
    }
}
