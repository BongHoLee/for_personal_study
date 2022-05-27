package shipInfo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ShipInfoTest {

    @Test
    void 유효한_정보_전달_및_생성_테스트() {
        new ShipInfo(
                new CustomerName("lee"),
                new Tel("010-5566-5081"),
                new Address("신길로 45길 7")
        );
    }

    @Test
    void 이름이_공백일시_예외발생_테스트() {
        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> {
                    new ShipInfo(
                            new CustomerName(""),
                            new Tel("010-555"),
                            new Address("신길로")
                    );
                }
        );
    }

}
