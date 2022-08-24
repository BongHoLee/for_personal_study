package com.realword.ch4;

import java.io.File;

public class ReportImporter implements FileImporter {
    private static final String EXTENSION = ".report";

    @Override
    public boolean canImport(String path) {
        File file = new File(path);
        return file.exists() && path.lastIndexOf(EXTENSION) != -1;
    }

    @Override
    public Document importFrom(String path) {
        if (!canImport(path)) {
            throw new IllegalArgumentException(path + " is cannot import using " + this.getClass().getName());
        }

        File reportFile = new File(path);

    }
}
