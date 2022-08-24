package com.realword.ch4;

import java.util.HashMap;
import java.util.Map;

public class Document {
    private final Map<String, String> indexed;

    public Document(Map<String, String> indexed) {
        this.indexed = indexed;
    }

    public Document() {
        this(new HashMap<>());
    }

    public String search(String query) {
        return indexed.get(query);
    }

    public boolean contains(String key) {
        return indexed.containsKey(key);
    }
}
