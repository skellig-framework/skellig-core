package org.skellig.teststep.processor.cassandra

import com.datastax.driver.core.Session
import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory

internal class CassandraRequestExecutorFactory(session: Session)
    : DatabaseRequestExecutorFactory(
        CassandraSelectRequestExecutor(session),
        CassandraInsertRequestExecutor(session))