package com.myshop.order.domain.receiver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class ReceiverTest {

    @Test
    void createTest() {
        new Receiver(
                new ReceiverName("leebongho"),
                new ReceiverAddress("신길로 45길")
        );
    }

    @Test
    void 동일한_상태를가진_Receiver_equals_true_테스트() {

        Receiver origin = new Receiver(
                new ReceiverName("leebongho"),
                new ReceiverAddress("신길로 45길")
        );

        Receiver target = new Receiver(
                new ReceiverName("leebongho"),
                new ReceiverAddress("신길로 45길")
        );

        assertThat(origin).isEqualTo(target);
    }

    @Test
    void 다른_상태를가진_Receiver_equals_false_테스트() {

        Receiver origin = new Receiver(
                new ReceiverName("leebongho"),
                new ReceiverAddress("신길로 45길1")
        );

        Receiver target = new Receiver(
                new ReceiverName("leebongho"),
                new ReceiverAddress("신길로 45길")
        );

        assertThat(origin).isNotEqualTo(target);
    }

    @Test
    void 생성자_파라미터_null일시_exception_발생_테스트() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Receiver(
                    new ReceiverName("leebongho"),
                    null
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Receiver(
                    null,
                    new ReceiverAddress("신길로 45길")
            );
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Receiver(
                    null,
                    null
            );
        });
    }
}
