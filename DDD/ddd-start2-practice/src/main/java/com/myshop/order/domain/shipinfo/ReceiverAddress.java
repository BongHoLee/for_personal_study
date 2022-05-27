package com.myshop.order.domain.shipinfo;

import java.util.Objects;

public class ReceiverAddress {
    private final String address;

    public ReceiverAddress(String address) {
        validationCheck(address);
        this.address = address;
    }

    private void validationCheck(String address) {
        if (address == null || address.isEmpty())
            throw new IllegalArgumentException("ADDRESS CANNOT BE NULL OR EMPTY");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReceiverAddress that = (ReceiverAddress) o;
        return address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
