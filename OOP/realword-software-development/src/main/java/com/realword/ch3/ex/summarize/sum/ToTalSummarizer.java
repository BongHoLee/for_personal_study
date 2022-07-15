package com.realword.ch3.ex.summarize.sum;

import com.realword.ch3.ex.processor.BankTransaction;



public class ToTalSummarizer implements BankTransactionSummarizer{

    @Override
    public double summarize(double total, BankTransaction transaction) {
        return total + transaction.getAmount();
    }
}
