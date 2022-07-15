package com.realword.ch3.ex;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.processor.BankTransaction;
import java.time.Month;
import java.util.List;

/**
 * Class desc.
 *
 * @author o118014_D
 * @since 2022-07-15
 */

public interface GodInterface {
    double calculateTotalAmount();
    double calculateTotalInMonth(Month month);
    double calculateTotalInJanuary();
    double calculateAverageAmount();
    double calculateAverageAmountForCategory(String category);
    List<BankTransaction> findTransactions(BankTransactionFilter filter);
}

interface CalculateTotalAmount {
    double calculateTotalAmount();
}

interface CalculateAverage {
    double calculateAverage();
}

interface CalculateTotalInMonth {
    double calculateTotalInMonth();
}