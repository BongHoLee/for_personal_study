package com.realword.ch3.ex.summarize.sum;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.processor.BankTransaction;


public class FilterSummarizer implements BankTransactionSummarizer{

    private final BankTransactionFilter filter;

    public FilterSummarizer(BankTransactionFilter filter) {
        this.filter = filter;
    }

    @Override
    public double summarize(double total, BankTransaction transaction) {
        return filter.isMatched(transaction) ? total + transaction.getAmount() : total;
    }
}
