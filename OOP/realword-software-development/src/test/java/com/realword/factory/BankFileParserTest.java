package com.realword.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.realword.ch3.BankTransaction;
import com.realword.ch3.factory.BankFileParser;
import java.util.List;
import org.junit.jupiter.api.Test;


public class BankFileParserTest {

    private final static String resources = "src/test/resources";
    @Test
    void parseTest() {
        List<BankTransaction> list = BankFileParser.parse(resources + "/bank-test.csv");
        assertThat(list.size()).isGreaterThan(0);
    }
}
