package org.skellig.connection.database;

import java.util.Map;

public abstract class BaseDatabaseRequestExecutor implements DatabaseRequestExecutor {

    private static final String VALUE = "value";

    protected Object getParameterValue(Object item) {
        if (item instanceof Map && ((Map) item).containsKey(VALUE)) {
            item = ((Map) item).get(VALUE);
        }
        return item;
    }

    @Override
    public void close() {

    }
}