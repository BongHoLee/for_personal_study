import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-04-11
 */

public class RoughPrintColor {
    final Map<String, Supplier<String>> colorProcessorMap;

    public RoughPrintColor() {
        this.colorProcessorMap = new HashMap<>();

        colorProcessorMap.put("RED", this::processRed);
        colorProcessorMap.put("BLACK", this::processBlack);
        colorProcessorMap.put("YELLOW", this::processYellow);
    }

    public String processColor(String color) {
        if (colorProcessorMap.containsKey(color)) {
            return colorProcessorMap.get(color).get();
        } else {
            throw new IllegalArgumentException(color + "is not supported");
        }
    }

    private String processRed() {
        return "RED is Processed";
    }

    private String processBlack() {
        return "BLACK is Processed";
    }

    private String processYellow() {
        return "YELLOW is Processed";
    }

}
