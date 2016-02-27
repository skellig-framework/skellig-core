package org.skellig.teststep.processing.converter;

import java.nio.charset.StandardCharsets;

public class TestStepResultToStringConverter implements TestStepResultConverter {

    @Override
    public Object convert(String convertFunction, Object result) {
        if (result instanceof byte[]) {
            return new String((byte[]) result, StandardCharsets.UTF_8);
        } else {
            return String.valueOf(result);
        }
    }

    @Override
    public String getConvertFunctionName() {
        return "string";
    }
}
