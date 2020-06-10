package org.skellig.connection.channel;

import java.util.Optional;

public interface ReadingChannel extends AutoCloseable {

    Optional<Object> read();

    @Override
    default void close() {
    }
}
