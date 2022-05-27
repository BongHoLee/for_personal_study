import java.util.function.Supplier;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-04-11
 */

public enum ColorType {
    RED("빨강", () ->  "RED is Processed"),
    BLACK("검정", () -> "BLACK is Processed"),
    YELLOW("노랑", () -> "YELLOW is Processed")
    ;

    private final Supplier<String> supplier;
    private final String title;

    ColorType(String title, Supplier<String> supplier) {
        this.title = title;
        this.supplier = supplier;
    }

    public String processColor() {
       return supplier.get();
    }

}
