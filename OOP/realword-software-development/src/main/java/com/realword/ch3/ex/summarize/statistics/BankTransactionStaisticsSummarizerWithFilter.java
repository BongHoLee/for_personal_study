package com.realword.ch3.ex.summarize.statistics;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.processor.BankTransaction;
import com.realword.ch3.ex.summarize.SummaryStatistics;

public class BankTransactionStaisticsSummarizerWithFilter implements BankTransactionStaisticsSummarizer {
    
    private final BankTransactionFilter filter;
    public BankTransactionStaisticsSummarizerWithFilter(BankTransactionFilter filter) {
        this.filter = filter;
    }

    @Override
    public SummaryStatistics summarize(SummaryStatistics total, BankTransaction transaction) {
        return filter.isMatched(transaction) ? total.calculateWith(transaction) : total;
    }
}
