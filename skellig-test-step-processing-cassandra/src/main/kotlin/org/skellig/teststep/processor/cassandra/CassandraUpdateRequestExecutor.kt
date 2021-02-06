package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.Session
import org.skellig.teststep.processor.db.model.DatabaseRequest

internal class CassandraUpdateRequestExecutor(private val session: Session,
                                              private val insertExecutor: CassandraInsertRequestExecutor,
                                              private val selectExecutor: CassandraSelectRequestExecutor)
    : BaseCassandraRequestExecutor() {

    override fun execute(databaseRequest: DatabaseRequest): Any? {
      return null
    }
}