package baseball;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ValidationUtilsTest {

    @Test
    public void 숫자범위가_1이상_9이하_검증() {
        assertThat(ValidationUtils.validNumber(9)).isTrue();
        assertThat(ValidationUtils.validNumber(1)).isTrue();
        assertThat(ValidationUtils.validNumber(0)).isFalse();
        assertThat(ValidationUtils.validNumber(10)).isFalse();
    }
}
