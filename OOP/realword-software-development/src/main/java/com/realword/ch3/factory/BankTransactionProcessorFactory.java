package com.realword.ch3.factory;

import com.realword.ch3.BankTransactionProcessor;
import com.realword.ch3.BankTransaction;
import java.util.List;


public class BankTransactionProcessorFactory {
    public static BankTransactionProcessor createWith(String filePath) {
        List<BankTransaction> transactions = BankFileParser.parse(filePath);
        return new BankTransactionProcessor(transactions);
    }
}
