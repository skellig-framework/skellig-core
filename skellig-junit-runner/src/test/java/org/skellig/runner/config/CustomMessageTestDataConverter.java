package org.skellig.runner.config;

import org.skellig.teststep.processing.converter.TestDataConverter;

import java.util.Map;
import java.util.stream.Collectors;

public class CustomMessageTestDataConverter implements TestDataConverter {

    @Override
    public Object convert(Object testData) {
        if (testData instanceof Map) {
            Object toCustomFormat = ((Map) testData).get("toCustomFormat");
            if (toCustomFormat instanceof Map) {
                return ((Map<String, Object>) toCustomFormat).entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .collect(Collectors.joining(",", "{", "}"));
            }
        }
        return testData;
    }
}
