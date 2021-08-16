package baseball;

public class ValidationUtils {

    private static final int MAX_VALUE = 10;
    private static final int MIN_VALUE = 0;

    public static boolean validNumber(int number) {
        return number < MAX_VALUE && number > MIN_VALUE;
    }
}
