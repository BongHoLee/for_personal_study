/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-04-11
 */

public class ColorProcessor {

    public String processColor(String color) {
        return ColorType.valueOf(color).processColor();
    }
}


