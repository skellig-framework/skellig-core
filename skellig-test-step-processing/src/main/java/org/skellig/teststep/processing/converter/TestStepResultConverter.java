package org.skellig.teststep.processing.converter;

public interface TestStepResultConverter {

    Object convert(String convertFunction, Object result);

    String getConvertFunctionName();
}
