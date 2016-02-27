package org.skellig.teststep.processing.converter;

import org.apache.commons.lang3.SerializationUtils;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Map;

class TestDataToBytesConverter implements TestDataConverter {

    private static final String VALUE = "value";
    private static final String TO_BYTES = "toBytes";

    public Object convert(Object testData) {
        if (testData instanceof Map) {
            Map<String, Object> valueAsMap = (Map<String, Object>) testData;
            if (valueAsMap.containsKey(TO_BYTES)) {
                Map<String, Object> toBytes = (Map<String, Object>) valueAsMap.get(TO_BYTES);
                Object value = toBytes.get(VALUE);

                if (value instanceof String) {
                    testData = ((String) value).getBytes(StandardCharsets.UTF_8);
                } else if (testData instanceof Serializable) {
                    testData = SerializationUtils.serialize((Serializable) testData);
                } else {
                    throw new TestDataConversionException(String.format("Failed to convert to bytes the test data: %s\n" +
                            "It must be either String or Serializable object", testData));
                }
            }
        }
        return testData;
    }
}