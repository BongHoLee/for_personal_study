package com.realword.factory;

import com.realword.AccountingManager;
import com.realword.BankTransaction;
import java.util.List;


public class AccountingManagerFactory {
    public static AccountingManager createWith(String filePath) {
        List<BankTransaction> transactions = BankFileParser.parse(filePath);
        return new AccountingManager(transactions);
    }
}
