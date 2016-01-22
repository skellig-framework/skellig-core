package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TableTestStepValueExtractorTest {

    private TableTestStepValueExtractor tableValueExtractor;

    @BeforeEach
    void setUp() {
        tableValueExtractor = new TableTestStepValueExtractor();
    }

    @Test
    @DisplayName("Extract from Map last String value")
    void testExtractFromMapStringValue() {
        assertEquals("v3", tableValueExtractor.extract(getTestMap(), "f1.f2.f3"));
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    void testExtractFromMap() {
        Object response = tableValueExtractor.extract(getTestMap(), "f1.f2");

        assertTrue(((Map) response).containsKey("f3"));
    }

    @Test
    @DisplayName("Extract from List of Map last String value")
    void testExtractFromMapAndList() {
        Map<Object, Object> testMap = getTestMap();
        testMap.put("f4", Collections.singletonList(Collections.singletonMap("f5","v5")));

        assertEquals("v5", tableValueExtractor.extract(Collections.singletonList(testMap), "[0].f4.[0].f5"));
    }

    private Map<Object, Object> getTestMap() {
        return new HashMap<Object, Object>() {
            {
                put("f1", new HashMap<Object, Object>() {
                    {
                        put("f2", new HashMap<Object, Object>() {
                            {
                                put("f3", "v3");
                            }
                        });
                    }
                });
            }
        };
    }
}