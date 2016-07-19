package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processor.db.BaseDatabaseRequestExecutor;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;

abstract class BaseJdbcRequestExecutor extends BaseDatabaseRequestExecutor {

    @Override
    protected Object getParameterValue(Object item) {
        item = super.getParameterValue(item);

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
