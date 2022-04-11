import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-04-11
 */

public class PrintColorTypeTest {

    private ColorProcessor colorProcessor;

    @BeforeEach
    public void initColorProcessor() {
        colorProcessor = new ColorProcessor();
    }

    @Test
    @DisplayName("문자열 전달 시 해당 문자열과 관련된 프로세스 결과를 출력한다.")
    public void processorTest() {
        Map<String, String> inoutMap = new HashMap<>();
        inoutMap.put("RED", "RED is Processed");
        inoutMap.put("BLACK", "BLACK is Processed");
        inoutMap.put("YELLOW", "YELLOW is Processed");

        inoutMap.forEach((color, printedResult) -> assertThat(colorProcessor.processColor(color)).isEqualTo(printedResult));
    }

}
