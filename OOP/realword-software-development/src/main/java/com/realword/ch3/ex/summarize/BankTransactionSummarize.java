package com.realword.ch3.ex.summarize;

import com.realword.ch3.ex.processor.BankTransaction;

@FunctionalInterface
public interface BankTransactionSummarize {
    SummaryStatistics summarize(SummaryStatistics total, BankTransaction transaction);
}
