package function;

public class PrintPrimes {
    private final int numberOfPrimes = 1000;

    public void main(String[] args) {
        PrimeGenerator primeGenerator = new PrimeGenerator(numberOfPrimes);
        NumberPrinter numberPrinter = new NumberPrinter();

        int[] primes = primeGenerator.generatePrimeNumbers();
        numberPrinter.printNumber(numberOfPrimes, primes);
    }


}
