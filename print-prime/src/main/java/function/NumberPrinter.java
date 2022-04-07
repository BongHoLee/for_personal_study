package function;

class NumberPrinter {


    public void printNumber(int numberOfPrimes, int[] primes) {
        int pagenumber = 1;
        int pageoffset = 1;
        int linesPerPage = 50;
        int columns = 4;

        while (pageoffset <= numberOfPrimes) {
            System.out.println("The First " + numberOfPrimes +
                " Prime Numbers --- Page " + pagenumber);
            System.out.println("");
            for (int rowoffset = pageoffset; rowoffset < pageoffset + linesPerPage; rowoffset++) {
                for (int column = 0; column < columns; column++) {
                    if (rowoffset + column * linesPerPage <= numberOfPrimes) {
                        System.out.format("%10d",
                            primes[rowoffset + column * linesPerPage]);
                    }
                }
                System.out.println("");
            }
            System.out.println("\f");
            pagenumber = pagenumber + 1;
            pageoffset = pageoffset + linesPerPage * columns;
        }
    }
}
