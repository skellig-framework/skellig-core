package org.skellig.teststep.processing.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ThreadLocalTestScenarioState implements TestScenarioState {

    private ThreadLocal<Map<String, Object>> state;

    public ThreadLocalTestScenarioState() {
        state = ThreadLocal.withInitial(HashMap::new);
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(state.get().get(key));
    }

    @Override
    public void set(String key, Object value) {
        state.get().put(key, value);
    }

    @Override
    public void remove(String key) {
        state.get().remove(key);
    }

    @Override
    public void clean() {
        state.get().clear();
    }
}
