package org.skellig.teststep.processor.cassandra

import org.skellig.teststep.processor.db.BaseDatabaseRequestExecutor
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

internal abstract class BaseCassandraRequestExecutor : BaseDatabaseRequestExecutor() {

    override fun getParameterValue(item: Any?): Any? {
        return when (val newItem = super.getParameterValue(item)) {
            is LocalDateTime -> Date.from(newItem.toInstant(ZoneOffset.UTC))
            is LocalDate -> Date.from(newItem.atStartOfDay(ZoneOffset.UTC).toInstant())
            else -> newItem
        }
    }
}