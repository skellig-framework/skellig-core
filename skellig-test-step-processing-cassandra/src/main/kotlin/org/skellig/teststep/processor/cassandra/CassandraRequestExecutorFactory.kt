package org.skellig.teststep.processor.cassandra

import com.datastax.oss.driver.api.core.CqlSession
import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory

internal class CassandraRequestExecutorFactory(session: CqlSession)
    : DatabaseRequestExecutorFactory(
        CassandraSelectRequestExecutor(session),
        CassandraInsertRequestExecutor(session),
        CassandraUpdateRequestExecutor(session,
                CassandraInsertRequestExecutor(session),
                CassandraSelectRequestExecutor(session)))