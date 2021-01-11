package org.skellig.teststep.processor.db

import org.skellig.teststep.processor.db.model.DatabaseRequest
import java.io.Closeable

interface DatabaseRequestExecutor : Closeable {

    fun execute(databaseRequest: DatabaseRequest): Any?

    override fun close()
}