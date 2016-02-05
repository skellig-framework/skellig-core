package org.skellig.teststep.processing.valueextractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;

class DefaultValueExtractorTest {

    private TestStepValueExtractor testStepValueExtractor;

    @BeforeEach
    void setUp() {
        testStepValueExtractor = new DefaultValueExtractor.Builder().build();
    }

    @Test
    void testExtractFromMap() {
        final String extractionParameter = "k2";
        Map<String, Object> value = createMap("k1", "v1", extractionParameter, "v2");

        assertEquals("v2", testStepValueExtractor.extract(value, extractionParameter));
    }

    @Test
    void testExtractFromJson() {
        String value = "{ \"params\" : { \"f1\" : \"v1\" }}";

        assertEquals("v1", testStepValueExtractor.extract(value, "json_path(params.f1)"));
    }

    @Test
    void testExtractFromRegex() {
        String value = "{ params = { k1 = v1 }}";

        assertEquals("v1", testStepValueExtractor.extract(value, "regex(k1 = (\\w+))"));
    }

    @Test
    void testExtractWhenExtractorIsNull() {
        String value = "{ params = { k1 = v1 }}";

        assertEquals(value, testStepValueExtractor.extract(value, null));
    }
}