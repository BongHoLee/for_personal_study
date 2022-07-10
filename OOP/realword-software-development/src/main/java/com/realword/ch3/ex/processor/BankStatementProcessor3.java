package com.realword.ch3.ex.processor;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.summarize.BankTransactionSummarize;
import com.realword.ch3.ex.summarize.SummaryStatistics;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class BankStatementProcessor3 {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor3(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    // 추상화된 BankTransactionSummarize에 의존
    public SummaryStatistics calculateTransactionsSummarizeAbout(BankTransactionSummarize summarizer) {
        SummaryStatistics summaryStatistics = new SummaryStatistics(0, 0, Double.MIN_VALUE, Double.MAX_VALUE, 0);
        for (BankTransaction bankTransaction : bankTransactions) {
            summaryStatistics = summarizer.summarize(summaryStatistics, bankTransaction);
        }
        return summaryStatistics;
    }

    // 추상화된 BankTransactionFilter에 의존
    public List<BankTransaction> findTransactionsAbout(BankTransactionFilter filter) {
        List<BankTransaction> result = new ArrayList<>();
        for (BankTransaction bankTransaction : bankTransactions) {
            if (filter.isMatched(bankTransaction)) {
                result.add(bankTransaction);
            }
        }

        return result;
    }
}
