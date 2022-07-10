package com.realword.ch3.ex.summarize;

import com.realword.ch3.ex.processor.BankTransaction;

public class SummaryStatistics {
    private final double count;
    private final double sum;
    private final double max;
    private final double min;
    private final double average;

    public SummaryStatistics(final double count, final double sum, final double max, final double min, final double average) {
        this.count = count;
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.average = average;
    }

    public double getSum() {
        return sum;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getAverage() {
        return average;
    }

    public double getCount() {
        return count;
    }

    public SummaryStatistics calculateWith(BankTransaction transaction) {
        double count = this.count + 1;
        double sum = this.sum + transaction.getAmount();
        double max = Math.max(this.max, transaction.getAmount());
        double min = Math.min(this.min, transaction.getAmount());
        double average = sum/count;
        return new SummaryStatistics(count, sum, max, min, average);
    }

    @Override
    public String toString() {
        return "SummaryStatistics{" +
                "count=" + count +
                ", sum=" + sum +
                ", max=" + max +
                ", min=" + min +
                ", average=" + average +
                '}';
    }
}
