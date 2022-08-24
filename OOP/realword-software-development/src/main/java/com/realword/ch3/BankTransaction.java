package com.realword.ch3;

import java.time.LocalDate;
import java.time.Month;
import java.util.Objects;

public class BankTransaction {
    private final LocalDate date;
    private final long amount;
    private final String category;

    public BankTransaction(LocalDate date, long amount, String category) {
        this.date = date;
        this.amount = amount;
        this.category = category;

    }

    public boolean isTransactedAt(Month month) {
        return date.getMonth() == month;
    }

    public long getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "BankTransaction{" +
                "date=" + date +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BankTransaction that = (BankTransaction) o;
        return getAmount() == that.getAmount() && date.equals(that.date) && getCategory().equals(
                that.getCategory());
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, getAmount(), getCategory());
    }
}
