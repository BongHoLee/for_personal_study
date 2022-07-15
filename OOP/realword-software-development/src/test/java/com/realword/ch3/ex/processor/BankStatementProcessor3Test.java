package com.realword.ch3.ex.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.filter.GreaterThanEqualAmountFilter;
import com.realword.ch3.ex.summarize.SummaryStatistics;
import com.realword.ch3.ex.summarize.statistics.BankTransactionStaisticsSummarizer;
import com.realword.ch3.ex.summarize.statistics.BankTransactionStaisticsSummarizerWithFilter;
import com.realword.ch3.ex.summarize.statistics.BankTransactionsTotalStaisticsSummarizer;
import com.realword.ch3.ex.summarize.sum.BankTransactionSummarizer;
import com.realword.ch3.ex.summarize.sum.FilterSummarizer;
import com.realword.ch3.ex.summarize.sum.ToTalSummarizer;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankStatementProcessor3Test {

    List<BankTransaction> transactions = new ArrayList<>();
    BankStatementProcessor3 processor;

    @BeforeEach
    void init() {
        transactions.add(new BankTransaction(LocalDate.of(2022, 1, 1), 100, "음식"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 2, 1), 200, "책"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 3, 1), 200, "책"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 4, 1), 100, "음식"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 4, 1), 300, "완구"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 1, 1), 300, "완구"));

        processor = new BankStatementProcessor3(transactions);
    }

    @Test
    void 액수가_200이상인_거래내역_조회() {
        // given
        BankTransactionFilter filter = new GreaterThanEqualAmountFilter(200);


        // when
        List<BankTransaction> filtered = processor.findTransactionsAbout(filter);

        // then
        assertThat(filtered.stream().allMatch(each -> each.getAmount() >= 200)).isTrue();


    }

    @Test
    void JANUARY_거래내역_통계() {
        // given
        BankTransactionFilter filter = (BankTransaction transaction) -> transaction.getDate().getMonth() == Month.JANUARY;


        // when
        List<BankTransaction> filtered = processor.findTransactionsAbout(filter);

        // then
        assertThat(filtered.stream().allMatch(each -> each.getDate().getMonth() == Month.JANUARY)).isTrue();
    }

    @Test
    void 모든_거래내역_통계() {
        // given
        BankTransactionSummarizer summarizer = new ToTalSummarizer();

        // when
        double total = processor.calculateTransactionsSummarizeAbout(summarizer);

        // then
        assertThat(total).isEqualTo(1200.0);
    }

    @Test
    void 완구_거래내역_통계() {
        // given
        BankTransactionFilter filter = (BankTransaction transaction) -> transaction.getDescription().equals("완구");
        BankTransactionSummarizer summarize = new FilterSummarizer(filter);

        // when
        double total = processor.calculateTransactionsSummarizeAbout(summarize);

        assertThat(total).isEqualTo(600.0);
    }
}