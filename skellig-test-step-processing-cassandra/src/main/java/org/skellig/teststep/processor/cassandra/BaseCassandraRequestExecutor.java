package org.skellig.teststep.processor.cassandra;

import org.skellig.teststep.processor.db.BaseDatabaseRequestExecutor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

abstract class BaseCassandraRequestExecutor extends BaseDatabaseRequestExecutor {

    @Override
    protected Object getParameterValue(Object item) {
        item = super.getParameterValue(item);

        if (item instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) item).toInstant(ZoneOffset.UTC));
        } else {
            return item;
        }
    }
}
