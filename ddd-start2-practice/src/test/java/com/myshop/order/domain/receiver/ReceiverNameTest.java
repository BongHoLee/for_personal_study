package com.myshop.order.domain.receiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ReceiverNameTest {

    @Test
    void 이름_공백일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReceiverName("");
        });
    }

    @Test
    void 이름_NULL일시_예외발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ReceiverName(null);
        });
    }

    @Test
    void 이름_동일시_equals_true_반환_테스트() {
        String name = "leebongho";
        ReceiverName origin = new ReceiverName(name);
        ReceiverName target = new ReceiverName(name);

        assertThat(origin).isEqualTo(target);
    }

}
