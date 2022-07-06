package com.realword;

import java.time.LocalDate;
import java.time.Month;

public class BankTransaction {
    private final LocalDate date;
    private final long amount;
    private final String description;

    public BankTransaction(LocalDate date, long amount, String description) {
        this.date = date;
        this.amount = amount;
        this.description = description;

    }

    public boolean isTransactedAt(Month month) {
        return date.getMonth() == month;
    }

    public long getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "BankTransaction{" +
                "date=" + date +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}
