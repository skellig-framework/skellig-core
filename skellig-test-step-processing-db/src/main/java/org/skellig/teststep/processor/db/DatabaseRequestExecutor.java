package org.skellig.teststep.processor.db;

import org.skellig.teststep.processor.db.model.DatabaseRequest;

public interface DatabaseRequestExecutor extends AutoCloseable {

    Object execute(DatabaseRequest databaseRequest);

    @Override
    void close();
}
