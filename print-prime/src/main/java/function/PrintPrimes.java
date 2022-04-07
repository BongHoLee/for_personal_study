package function;

public class PrintPrimes {
    private final int numberOfPrimes = 1000;

    public void main(String[] args) {
        PrintPrimeHelper printPrimeHelper = new PrintPrimeHelper(numberOfPrimes);
        int[] primes = printPrimeHelper.invoke();
        NumberPrinter numberPrinter = new NumberPrinter(numberOfPrimes, primes);
        numberPrinter.printNumber();
    }


}
