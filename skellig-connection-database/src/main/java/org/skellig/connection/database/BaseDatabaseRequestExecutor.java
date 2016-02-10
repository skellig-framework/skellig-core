package org.skellig.connection.database;

import org.skellig.connection.database.model.DatabaseRequest;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

abstract class BaseDatabaseRequestExecutor {

    private static final String VALUE = "value";

    abstract Object executeRequest(Connection connection, DatabaseRequest databaseRequest);

    protected Object getParameterValue(Object item) {
        if (item instanceof Map && ((Map) item).containsKey(VALUE)) {
            item = ((Map) item).get(VALUE);
        }

        if (item instanceof LocalDateTime) {
            return fromLocalDateTimeToTimestamp((LocalDateTime) item);
        } else if (item instanceof Instant) {
            return fromInstantToTimestamp((Instant) item);
        } else {
            return item;
        }
    }

    private Timestamp fromLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        } else {
            return Timestamp.valueOf(localDateTime);
        }
    }

    private Timestamp fromInstantToTimestamp(Instant instant) {
        return fromLocalDateTimeToTimestamp(LocalDateTime.from(instant));
    }
}
