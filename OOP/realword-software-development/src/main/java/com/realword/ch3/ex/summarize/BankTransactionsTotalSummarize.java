package com.realword.ch3.ex.summarize;

import com.realword.ch3.ex.processor.BankTransaction;

public class BankTransactionsTotalSummarize implements BankTransactionSummarize{

    @Override
    public SummaryStatistics summarize(SummaryStatistics total, BankTransaction transaction) {
        return total.calculateWith(transaction);
    }
}
