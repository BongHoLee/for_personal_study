package com.myshop.order.domain.shipinfo;

import java.util.Objects;

public class ReceiverTel {
    private final String tel;
    public ReceiverTel(String tel) {
        validationCheck(tel);
        this.tel = tel;
    }

    private void validationCheck(String tel) {
        if (tel == null || tel.isEmpty())
            throw new IllegalArgumentException("TEL CANNOT BE NULL OR EMPTY");
    }

    public String getTel() {
        return tel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReceiverTel that = (ReceiverTel) o;
        return tel.equals(that.tel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tel);
    }
}
