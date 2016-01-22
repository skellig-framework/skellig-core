package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegexTestStepValueExtractorTest {

    private RegexTestStepValueExtractor regexValueExtractor;

    @BeforeEach
    void setUp() {
        regexValueExtractor = new RegexTestStepValueExtractor();
    }

    @Test
    void testExtractByRegex() {
        String regexFilter = ".*id\\s*=\\s*([A-Z]{2}\\d{4}).*";

        assertEquals("NM1100",
                regexValueExtractor.extract("log data: id = NM1100, name = event", regexFilter));
    }

    @Test
    void testExtractByRegexWhenNoMatch() {
        String regexFilter = "data: ([a-z]+)";
        String value = "data: 1000";

        assertEquals(value, regexValueExtractor.extract(value, regexFilter));
    }
}