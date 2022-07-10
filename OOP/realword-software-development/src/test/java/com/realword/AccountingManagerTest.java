package com.realword;


import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
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
        transactions.add(new BankTransaction(LocalDate.parse("01-03-2020", pattern), -20, "E"));
        transactions.add(new BankTransaction(LocalDate.parse("01-04-2020", pattern), -50, "E"));


        accountingManager = new AccountingManager(transactions);
    }

    @Test
    void totalAmountCalculateTest() {
        long streamResult = transactions.stream().mapToLong(BankTransaction::getAmount).reduce(Long::sum).orElse(0);
        long accountManagerTotalAmount = accountingManager.totalAmount();

        assertThat(streamResult).isEqualTo(accountManagerTotalAmount);
    }

    @Test
    void totalDepositTest() {
        assertThat(accountingManager.totalDeposit()).isEqualTo(100L);
    }

    @Test
    void totalWithdrawTest() {
        assertThat(accountingManager.totalWithdraw()).isEqualTo(-70L);
    }

    @Test
    void orderedWithdrawListTest() {
        List<BankTransaction> bankTransactions = accountingManager.orderedWithdrawListSizeOf(10);

        for (int i = 0; i < bankTransactions.size()-1; i++) {
            assertThat(bankTransactions.get(i).getAmount()).isGreaterThanOrEqualTo(bankTransactions.get(i+1).getAmount());
        }

    }

    @Test
    void mostWithdrawCategoryTest() {
        assertThat(accountingManager.mostWithdrawCategory()).isEqualTo("E");
    }

    @Test
    void calcuateOfMonthTest() {
        assertThat(accountingManager.calculateOf(Month.JANUARY)).isEqualTo(30);
        assertThat(accountingManager.calculateOf(Month.FEBRUARY)).isEqualTo(70);
        assertThat(accountingManager.calculateOf(Month.MARCH)).isEqualTo(-20);
        assertThat(accountingManager.calculateOf(Month.APRIL)).isEqualTo(-50);
    }

}