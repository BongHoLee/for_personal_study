package com.realword.ch3.ex.summarize.sum;


import com.realword.ch3.ex.processor.BankTransaction;

@FunctionalInterface
public interface BankTransactionSummarizer {
    double summarize(double total, BankTransaction transaction);
}
