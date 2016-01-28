package org.skellig.teststep.processing.valueextractor;

public interface TestStepValueExtractor {

    Object extract(Object value, String extractionParameter);

    String getExtractFunctionName();
}
