package orderline.product;

public class Volume {
    private static final long MIN_VALUE = 0;
    private static final String VALUE_EXCEPTION_MESSAGE = "NOT_VALID_VOLUME";
    private final long volume;

    public static Volume of(long volume) {
        return new Volume(volume);
    }

    private Volume(long volume) {
        validationCheck(volume);
        this.volume = volume;
    }

    private void validationCheck(long volume) {
        if (volume < MIN_VALUE) {
            throw new IllegalArgumentException(VALUE_EXCEPTION_MESSAGE);
        }
    }

    public long getVolume() {
        return this.volume;
    }

}
