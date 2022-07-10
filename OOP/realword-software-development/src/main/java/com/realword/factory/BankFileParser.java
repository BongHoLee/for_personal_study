package com.realword.factory;

import com.realword.BankTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class BankFileParser {
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private BankFileParser() {}
    public static List<BankTransaction> parse(String filePath) {
        List<BankTransaction> bankTransactions = new ArrayList<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitString = line.split(",");
                bankTransactions.add(parseFromCSV(splitString));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bankTransactions;
    }

    private static BankTransaction parseFromCSV(String[] splitString) {
        return new BankTransaction(getDate(splitString[0]), getAmount(splitString[1]), getDescription(splitString[2]));
    }
    private static LocalDate getDate(String dateString) {
        return LocalDate.parse(dateString, DATE_PATTERN);
    }

    private static long getAmount(String amountString) {
        return Long.parseLong(amountString);
    }

    private static String getDescription(String descriptionString) {
        return descriptionString;
    }
}
