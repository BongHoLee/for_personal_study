package orderline.product;

public class Price {
    private static final long MIN_VALUE = 0;
    private static final String MIN_VALUE_EXCEPTION_MESSAGE = "NOT_VALID_VALUES_FOR_PRICE";
    private final long value;

    public static Price of(long value) {
        validationCheck(value);
        return new Price(value);
    }

    private static void validationCheck(long value) {
        if (value < MIN_VALUE) {
            throw new IllegalArgumentException(MIN_VALUE_EXCEPTION_MESSAGE);
        }
    }

    private Price(long value) {
        this.value = value;
    }

    public long value() {
        return this.value;
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    public Price sumWith(Price target) {
        long originValue = this.value;
        long targetValue = target.value();
        return of(originValue + targetValue);
    }
}
