package org.skellig.teststep.processing.converter;

public interface ValueExtractor {

    Object extract(Object value, String filter);

    String getExtractFunctionName();
}
