package com.realword;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccountingManagerTest {

    private  AccountingManager accountingManager;
    private List<BankTransaction> transactions = new ArrayList<>();
    @BeforeEach
    void createManager() {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        transactions.add(new BankTransaction(LocalDate.parse("01-01-2020", pattern), 10, "A"));
        transactions.add(new BankTransaction(LocalDate.parse("01-01-2020", pattern), 20, "B"));
        transactions.add(new BankTransaction(LocalDate.parse("01-02-2020", pattern), 30, "C"));
        transactions.add(new BankTransaction(LocalDate.parse("01-02-2020", pattern), 40, "D"));
        transactions.add(new BankTransaction(LocalDate.parse("01-03-2020", pattern), 50, "E"));

        accountingManager = new AccountingManager(transactions);
    }

    @Test
    void totalAmountCalculateTest() {
        long streamResult = transactions.stream().mapToLong(BankTransaction::getAmount).reduce(Long::sum).orElse(0);
        long accountManagerTotalAmount = accountingManager.totalAmount();

        assertThat(streamResult).isEqualTo(accountManagerTotalAmount);
    }

}