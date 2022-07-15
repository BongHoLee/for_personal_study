package com.realword.ch3.ex.processor;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.summarize.sum.BankTransactionSummarizer;
import java.util.ArrayList;
import java.util.List;

public class BankStatementProcessor3 {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor3(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    // 추상화된 BankTransactionSummarizer에 의존
    public double calculateTransactionsSummarizeAbout(BankTransactionSummarizer summarizer) {
        double total = 0;
        for (BankTransaction bankTransaction : bankTransactions) {
            total = summarizer.summarize(total, bankTransaction);
        }
        return total;
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
