package org.skellig.teststep.processor.jdbc;

import org.skellig.teststep.processor.db.BaseDatabaseRequestExecutor;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

abstract class BaseJdbcRequestExecutor extends BaseDatabaseRequestExecutor {

    @Override
    protected Object getParameterValue(Object item) {
        item = super.getParameterValue(item);

        if (item instanceof LocalDateTime) {
            return fromLocalDateTimeToTimestamp((LocalDateTime) item);
        } else if (item instanceof LocalDate) {
            return fromLocalDateToSqlDate((LocalDate) item);
        } else if (item instanceof Instant) {
            return fromInstantToTimestamp((Instant) item);
        } else {
            return item;
        }
    }

    private Timestamp fromLocalDateTimeToTimestamp(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime);
    }

    private Date fromLocalDateToSqlDate(LocalDate localDate) {
        return Date.valueOf(localDate);
    }

    private Timestamp fromInstantToTimestamp(Instant instant) {
        return fromLocalDateTimeToTimestamp(LocalDateTime.from(instant));
    }
}
