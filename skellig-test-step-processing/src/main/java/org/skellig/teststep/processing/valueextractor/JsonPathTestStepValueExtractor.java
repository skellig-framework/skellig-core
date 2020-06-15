package org.skellig.teststep.processing.valueextractor;

import io.restassured.path.json.JsonPath;

import static io.restassured.path.json.JsonPath.from;

class JsonPathTestStepValueExtractor implements TestStepValueExtractor {

    @Override
    public Object extract(Object value, String extractionParameter) {
        JsonPath json = from((String) value);
        return json.getString(extractionParameter);
    }

    @Override
    public String getExtractFunctionName() {
        return "json_path";
    }

}
