package com.myshop.order.domain.receiver;

import java.util.Objects;

public class Receiver {
    private final ReceiverName receiverName;
    private final ReceiverAddress receiverAddress;

    public Receiver(ReceiverName receiverName, ReceiverAddress receiverAddress) {
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
        Receiver receiver = (Receiver) o;
        return getReceiverName().equals(receiver.getReceiverName()) && getReceiverAddress().equals(
                receiver.getReceiverAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReceiverName(), getReceiverAddress());
    }
}
