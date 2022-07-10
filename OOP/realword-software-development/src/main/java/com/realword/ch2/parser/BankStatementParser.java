package com.realword.ch2.parser;

import com.realword.ch2.processor.BankTransaction;
import java.util.List;

public interface BankStatementParser {
    BankTransaction parseFrom(String line);
    List<BankTransaction> parseLinesFrom(List<String> lines);
}
