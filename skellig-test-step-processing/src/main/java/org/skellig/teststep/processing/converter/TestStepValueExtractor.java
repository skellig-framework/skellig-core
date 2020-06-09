package org.skellig.teststep.processing.converter;

public interface TestStepValueExtractor {

    Object extract(Object value, String filter);

    String getExtractFunctionName();
}
