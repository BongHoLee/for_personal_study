package com.myshop.order.domain.shipinfo;

import java.util.Objects;

public class ReceiverName {
    private final String name;

    public ReceiverName(String name) {
        validationCheck(name);
        this.name = name;
    }

    private void validationCheck(String name) {
        if (name == null || name.isEmpty())
            throw new IllegalArgumentException("NAME CANNOT BE EMPTY OR NULL");
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReceiverName that = (ReceiverName) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName());
    }
}
