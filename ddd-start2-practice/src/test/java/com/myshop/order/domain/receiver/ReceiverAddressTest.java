package com.myshop.order.domain.receiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ReceiverAddressTest {

    @Test
    void 주소_공백_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReceiverAddress("");
        });
    }

    @Test
    void 주소_null_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReceiverAddress(null);
        });
    }

    @Test
    void 같은_주소일시_equals_true_반환_테스트() {
        String address = "신길로45길 7";
        ReceiverAddress origin = new ReceiverAddress(address);
        ReceiverAddress target = new ReceiverAddress(address);

        assertThat(origin).isEqualTo(target);
    }

}
