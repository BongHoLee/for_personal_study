package com.realword.ch3.ex.filter;

import com.realword.ch3.ex.processor.BankTransaction;

public class GreaterThanEqualAmountFilter implements BankTransactionFilter{

    private final double amount;

    public GreaterThanEqualAmountFilter(double amount) {
        this.amount = amount;
    }

    @Override
    public boolean isMatched(BankTransaction bankTransaction) {
        return bankTransaction.getAmount() >= amount;
    }
}
