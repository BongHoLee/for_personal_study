package com.realword.ch2;

import com.realword.ch2.analyzer.BankStatementAnalyzer;
import com.realword.ch2.parser.BankStatementCSVParser;
import com.realword.ch2.parser.BankStatementParser;

public class MainApplication {

    public static void main(String[] args) throws Exception {

        final BankStatementAnalyzer bankStatementAnalyzer
                = new BankStatementAnalyzer();

        final BankStatementParser bankStatementParser
                = new BankStatementCSVParser();

        bankStatementAnalyzer.analyze("bank-data-simple.csv", bankStatementParser);

    }
}
