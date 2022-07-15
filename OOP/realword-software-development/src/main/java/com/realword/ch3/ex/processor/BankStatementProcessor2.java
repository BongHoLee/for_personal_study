package com.realword.ch3.ex.processor;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

public class BankStatementProcessor2 {
    private final List<BankTransaction> bankTransactions;

    public BankStatementProcessor2(final List<BankTransaction> bankTransactions) {
        this.bankTransactions = bankTransactions;
    }

    // 거래 내역의 총 액수
    public double calculateTotalAmount() {
        double total = 0;
        for (BankTransaction bankTransaction : bankTransactions) {
            total += bankTransaction.getAmount();
        }
        return total;
    }

    // 특정 달의 거래 내역 총 액수
    public double calculateTotalInMonth(final Month month) {
        double total = 0;
        for (BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDate().getMonth() == month) {

                total += bankTransaction.getAmount();
            }
        }
        return total;
    }

    // 특정 항목의 거래 내역 총 액수
    public double calculateTotalForCategory(final String category) {
        double total = 0;
        for (BankTransaction bankTransaction : bankTransactions) {
            if (bankTransaction.getDescription().equals(category)) {
                total += bankTransaction.getAmount();
            }
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
