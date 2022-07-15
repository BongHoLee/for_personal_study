package com.realword.ch3.ex.summarize.statistics;

import com.realword.ch3.ex.processor.BankTransaction;
import com.realword.ch3.ex.summarize.SummaryStatistics;

@FunctionalInterface
public interface BankTransactionStaisticsSummarizer {
    SummaryStatistics summarize(SummaryStatistics total, BankTransaction transaction);
}
