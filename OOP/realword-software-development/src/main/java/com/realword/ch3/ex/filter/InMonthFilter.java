package com.realword.ch3.ex.filter;

import com.realword.ch3.ex.processor.BankTransaction;
import java.time.Month;

public class InMonthFilter implements BankTransactionFilter{

    private final Month month;

    public InMonthFilter(Month month) {
        this.month = month;
    }

    @Override
    public boolean isMatched(BankTransaction bankTransaction) {
        return bankTransaction.getDate().getMonth() == month;
    }
}
