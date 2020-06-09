package org.skellig.teststep.processing.converter;

import io.restassured.path.json.JsonPath;

import static io.restassured.path.json.JsonPath.from;

class JsonPathTestStepValueExtractor implements TestStepValueExtractor {

    @Override
    public Object extract(Object value, String filter) {
        JsonPath json = from((String) value);
        return json.getString(filter);
    }

    @Override
    public String getExtractFunctionName() {
        return "json_path";
    }

}
