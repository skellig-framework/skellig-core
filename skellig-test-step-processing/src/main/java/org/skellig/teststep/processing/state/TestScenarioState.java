package org.skellig.teststep.processing.state;

import java.util.Optional;

public interface TestScenarioState {

    Optional<Object> get(String key);

    void set(String key, Object value);

    void remove(String key);

    void clean();
}
