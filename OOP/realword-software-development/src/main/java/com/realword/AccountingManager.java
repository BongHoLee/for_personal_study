package com.realword;

import java.util.List;


public class AccountingManager {
    private final List<BankTransaction> transactions;

    public AccountingManager(List<BankTransaction> transactions) {
        this.transactions = transactions;
    }

    public long totalAmount() {
        return transactions.stream().mapToLong(BankTransaction::getAmount).reduce(Long::sum).orElse(0);
    }
}
