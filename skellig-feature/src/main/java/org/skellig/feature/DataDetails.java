package org.skellig.feature;

import java.util.stream.Stream;

public class DataDetails implements TestPreRequisites<DataDetails> {

    private final String[] paths;

    public DataDetails(String[] paths) {
        this.paths = Stream.of(paths).map(String::trim).toArray(String[]::new);
    }

    public String[] getPaths() {
        return paths;
    }

    @Override
    public DataDetails getDetails() {
        return this;
    }
}
