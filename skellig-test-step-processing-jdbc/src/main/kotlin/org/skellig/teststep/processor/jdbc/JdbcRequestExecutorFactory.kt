package org.skellig.teststep.processor.jdbc

import org.skellig.teststep.processor.db.DatabaseRequestExecutorFactory
import java.sql.Connection

internal class JdbcRequestExecutorFactory(connection: Connection?)
    : DatabaseRequestExecutorFactory(
        JdbcSelectRequestExecutor(connection),
        JdbcInsertRequestExecutor(connection),
        JdbcUpdateRequestExecutor(connection,
                JdbcSelectRequestExecutor(connection),
                JdbcInsertRequestExecutor(connection))
)