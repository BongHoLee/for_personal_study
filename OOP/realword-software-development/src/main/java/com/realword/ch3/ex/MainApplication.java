package com.realword.ch3.ex;

import com.realword.ch3.ex.analyzer.BankStatementAnalyzer;
import com.realword.ch3.ex.parser.BankStatementCSVParser;
import com.realword.ch3.ex.parser.BankStatementParser;

public class MainApplication {

    public static void main(String[] args) throws Exception {

        final BankStatementAnalyzer bankStatementAnalyzer
                = new BankStatementAnalyzer();

        final BankStatementParser bankStatementParser
                = new BankStatementCSVParser();

        bankStatementAnalyzer.analyze("bank-data-simple.csv", bankStatementParser);

    }
}
