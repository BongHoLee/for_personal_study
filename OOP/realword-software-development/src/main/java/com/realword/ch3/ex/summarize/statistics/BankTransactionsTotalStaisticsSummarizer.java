package com.realword.ch3.ex.summarize.statistics;

import com.realword.ch3.ex.processor.BankTransaction;
import com.realword.ch3.ex.summarize.SummaryStatistics;

public class BankTransactionsTotalStaisticsSummarizer implements BankTransactionStaisticsSummarizer {

    @Override
    public SummaryStatistics summarize(SummaryStatistics total, BankTransaction transaction) {
        return total.calculateWith(transaction);
    }
}
