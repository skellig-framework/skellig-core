package org.skellig.teststep.processing.state;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTestScenarioState implements TestScenarioState {

    private Map<String, Object> state;

    public DefaultTestScenarioState() {
        state = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<Object> get(String key) {
        return key == null ? Optional.empty() : Optional.ofNullable(state.get(key));
    }

    @Override
    public void set(String key, Object value) {
        if(value != null) {
            state.put(key, value);
        }
    }

    @Override
    public void remove(String key) {
        if(key != null) {
            state.remove(key);
        }
    }

    @Override
    public void clean() {
        state.clear();
    }
}
