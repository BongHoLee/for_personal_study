package com.realword.ch3.ex.processor;

import static org.assertj.core.api.Assertions.assertThat;

import com.realword.ch3.ex.filter.BankTransactionFilter;
import com.realword.ch3.ex.filter.GreaterThanEqualAmountFilter;
import com.realword.ch3.ex.summarize.statistics.BankTransactionStaisticsSummarizer;
import com.realword.ch3.ex.summarize.statistics.BankTransactionStaisticsSummarizerWithFilter;
import com.realword.ch3.ex.summarize.statistics.BankTransactionsTotalStaisticsSummarizer;
import com.realword.ch3.ex.summarize.SummaryStatistics;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BankStatementProcessor4Test {

    List<BankTransaction> transactions = new ArrayList<>();
    BankStatementProcessor4 processor;

    @BeforeEach
    void init() {
        transactions.add(new BankTransaction(LocalDate.of(2022, 1, 1), 100, "음식"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 2, 1), 200, "책"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 3, 1), 200, "책"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 4, 1), 100, "음식"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 4, 1), 300, "완구"));
        transactions.add(new BankTransaction(LocalDate.of(2022, 1, 1), 300, "완구"));

        processor = new BankStatementProcessor4(transactions);
    }

    @Test
    void 액수가_200이상인_거래내역_통계() {
        // given
        BankTransactionFilter filter = new GreaterThanEqualAmountFilter(200);
        BankTransactionStaisticsSummarizerWithFilter summarize = new BankTransactionStaisticsSummarizerWithFilter(filter);

        // when
        SummaryStatistics summaryStatistics = processor.calculateTransactionsSummarizeAbout(summarize);


        // then
        assertThat(summaryStatistics.getCount()).isEqualTo(4.0);
        assertThat(summaryStatistics.getSum()).isEqualTo(1000.0);
        assertThat(summaryStatistics.getMax()).isEqualTo(300.0);
        assertThat(summaryStatistics.getMin()).isEqualTo(200.0);
        assertThat(summaryStatistics.getAverage()).isEqualTo(250.0);
    }

    @Test
    void JANUARY_거래내역_통계() {
        // given
        BankTransactionFilter filter = (BankTransaction transaction) -> transaction.getDate().getMonth() == Month.JANUARY;
        BankTransactionStaisticsSummarizerWithFilter summarize = new BankTransactionStaisticsSummarizerWithFilter(filter);

        // when
        SummaryStatistics summaryStatistics = processor.calculateTransactionsSummarizeAbout(summarize);


        // then
        assertThat(summaryStatistics.getCount()).isEqualTo(2.0);
        assertThat(summaryStatistics.getSum()).isEqualTo(400.0);
        assertThat(summaryStatistics.getMax()).isEqualTo(300.0);
        assertThat(summaryStatistics.getMin()).isEqualTo(100.0);
        assertThat(summaryStatistics.getAverage()).isEqualTo(200.0);
    }

    @Test
    void 모든_거래내역_통계() {
        // given
        BankTransactionStaisticsSummarizer summarize = new BankTransactionsTotalStaisticsSummarizer();

        // when
        SummaryStatistics summaryStatistics = processor.calculateTransactionsSummarizeAbout(summarize);

        assertThat(summaryStatistics.getCount()).isEqualTo(6.0);
        assertThat(summaryStatistics.getSum()).isEqualTo(1200.0);
        assertThat(summaryStatistics.getMax()).isEqualTo(300.0);
        assertThat(summaryStatistics.getMin()).isEqualTo(100.0);
        assertThat(summaryStatistics.getAverage()).isEqualTo(200.0);
    }

    @Test
    void 완구_거래내역_통계() {
        // given
        BankTransactionFilter filter = (BankTransaction transaction) -> transaction.getDescription().equals("완구");
        BankTransactionStaisticsSummarizerWithFilter summarize = new BankTransactionStaisticsSummarizerWithFilter(filter);

        // when
        SummaryStatistics summaryStatistics = processor.calculateTransactionsSummarizeAbout(summarize);


        assertThat(summaryStatistics.getCount()).isEqualTo(2.0);
        assertThat(summaryStatistics.getSum()).isEqualTo(600.0);
        assertThat(summaryStatistics.getMax()).isEqualTo(300.0);
        assertThat(summaryStatistics.getMin()).isEqualTo(300.0);
        assertThat(summaryStatistics.getAverage()).isEqualTo(300.0);
    }
}