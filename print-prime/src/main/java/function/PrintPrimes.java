package function;

public class PrintPrimes {
    private final int numberOfPrimes = 1000;

    public void main(String[] args) {
        PrintPrimeHelper printPrimeHelper = new PrintPrimeHelper(numberOfPrimes);
        NumberPrinter numberPrinter = new NumberPrinter();

        int[] primes = printPrimeHelper.invoke();
        numberPrinter.printNumber(numberOfPrimes, primes);
    }


}
