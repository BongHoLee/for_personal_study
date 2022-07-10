package com.realword.ch3.ex.filter;

import com.realword.ch3.ex.processor.BankTransaction;

@FunctionalInterface
public interface BankTransactionFilter {
    boolean isMatched(BankTransaction bankTransaction);
}
