package com.myshop.order.domain.shipinfo;

import java.util.Objects;

public class ShipInfo {
    private final ReceiverName receiverName;
    private final ReceiverAddress receiverAddress;
    private final ReceiverTel receiverTel;



    public ShipInfo(ReceiverName receiverName, ReceiverAddress receiverAddress, ReceiverTel receiverTel) {
        checkValidation(receiverName, receiverAddress, receiverTel);
        this.receiverName = receiverName;
        this.receiverAddress = receiverAddress;
        this.receiverTel = receiverTel;
    }

    private void checkValidation(ReceiverName receiverName, ReceiverAddress receiverAddress, ReceiverTel receiverTel) {
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
                shipInfo.getReceiverAddress()) && receiverTel.equals(shipInfo.receiverTel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReceiverName(), getReceiverAddress(), receiverTel);
    }
}
