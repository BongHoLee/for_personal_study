package com.realword;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.time.Month;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;


public class BankTransactionProcessor {
    private final List<BankTransaction> transactions;

    public BankTransactionProcessor(List<BankTransaction> transactions) {
        this.transactions = transactions;
    }

    public long totalAmount() {
        return totalDeposit() + totalWithdraw();
    }

    public long totalDeposit() {
        return calculateTotalAmountWithFilter((BankTransaction transaction) -> transaction.getAmount() > 0);
    }

    public long totalWithdraw() {
        return calculateTotalAmountWithFilter((BankTransaction transaction) -> transaction.getAmount() < 0);
    }

    public long calculateMonthOf(Month month) {
        return calculateTotalAmountWithFilter((BankTransaction transaction) -> transaction.isTransactedAt(month));
    }

    public long calculateCategoryOf(String category) {
        return calculateTotalAmountWithFilter((BankTransaction transaction) -> transaction.getCategory().equals(category));
    }

    private long calculateTotalAmountWithFilter(Predicate<BankTransaction> predicate) {
        return transactions.stream()
                .filter(predicate)
                .mapToLong(BankTransaction::getAmount)
                .reduce(Long::sum)
                .orElse(0);
    }

    public List<BankTransaction> orderedWithdrawListSizeOf(int size) {
        return transactions.stream()
                .sorted((o1, o2) -> (int)( o2.getAmount() - o1.getAmount()))
                .limit(size)
                .collect(toList());
    }

    public String mostWithdrawCategory() {
        return transactions.stream()
                .collect(groupingBy(BankTransaction::getCategory))
                .entrySet().stream()
                .collect(toMap(Entry::getKey,
                        eachEntry -> eachEntry.getValue().stream().map(BankTransaction::getAmount).reduce(Long::sum)
                                .orElse(0L)))
                .entrySet().stream().reduce((e1, e2) -> e1.getValue() > e2.getValue() ? e2 : e1).orElseThrow().getKey();
    }


}
