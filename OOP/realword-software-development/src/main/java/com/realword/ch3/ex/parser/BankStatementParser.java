package com.realword.ch3.ex.parser;

import com.realword.ch3.ex.processor.BankTransaction;
import java.util.List;

public interface BankStatementParser {
    BankTransaction parseFrom(String line);
    List<BankTransaction> parseLinesFrom(List<String> lines);
}
