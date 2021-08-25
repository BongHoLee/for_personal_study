package orderline.product;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VolumeTest {

    @Test
    void 정상_생성_테스트() {
        long value = 10;
        Volume volume = Volume.of(value);
        assertThat(volume.getVolume()).isEqualTo(value);
    }

    @Test
    void 음수_값_예외_반환_테스트() {
        long value = -1;
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Volume.of(value);
        });
    }
}
