package org.skellig.connection.channel;

import java.util.Optional;

public interface SendingChannel extends AutoCloseable {

    Optional<Object> send(Object request);

    @Override
    default void close() {
    }
}
