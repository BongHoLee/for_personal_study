package com.realword.ch4;

public interface FileImporter {
    boolean canImport(String path);
    Document importFrom(String path);
}
