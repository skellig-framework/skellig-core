package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processor.db.BaseDatabaseRequestExecutor
import java.sql.Date
import java.sql.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime

internal abstract class BaseJdbcRequestExecutor : BaseDatabaseRequestExecutor() {

    override fun getParameterValue(item: Any?): Any? {
        var newItem = item
        newItem = super.getParameterValue(newItem)
        return when (item) {
            is LocalDateTime -> fromLocalDateTimeToTimestamp(newItem!! as LocalDateTime)
            is LocalDate -> fromLocalDateToSqlDate(newItem!! as LocalDate)
            is Instant -> fromInstantToTimestamp(newItem!! as Instant)
            else -> newItem
        }
    }

    private fun fromLocalDateTimeToTimestamp(localDateTime: LocalDateTime): Timestamp {
        return Timestamp.valueOf(localDateTime)
    }

    private fun fromLocalDateToSqlDate(localDate: LocalDate): Date {
        return Date.valueOf(localDate)
    }

    private fun fromInstantToTimestamp(instant: Instant): Timestamp {
        return fromLocalDateTimeToTimestamp(LocalDateTime.from(instant))
    }
}