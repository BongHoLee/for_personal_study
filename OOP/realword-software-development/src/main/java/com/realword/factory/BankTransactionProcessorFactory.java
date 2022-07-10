package com.realword.factory;

import com.realword.BankTransactionProcessor;
import com.realword.BankTransaction;
import java.util.List;


public class BankTransactionProcessorFactory {
    public static BankTransactionProcessor createWith(String filePath) {
        List<BankTransaction> transactions = BankFileParser.parse(filePath);
        return new BankTransactionProcessor(transactions);
    }
}
