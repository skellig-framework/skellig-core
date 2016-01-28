package org.skellig.teststep.processing.valueextractor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.ValueExtractionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectTestStepValueExtractorTest {

    private ObjectTestStepValueExtractor valueExtractor;

    @BeforeEach
    void setUp() {
        valueExtractor = new ObjectTestStepValueExtractor();
    }

    @Test
    @DisplayName("Extract from Map last String value")
    void testExtractFromMapStringValue() {
        assertEquals("v3", valueExtractor.extract(getTestMap(), "f1.f2.f3"));
    }

    @Test
    @DisplayName("Extract from Map internal Map")
    void testExtractFromMap() {
        Object response = valueExtractor.extract(getTestMap(), "f1.f2");

        assertTrue(((Map) response).containsKey("f3"));
    }

    @Test
    @DisplayName("Extract from List of Map last String value")
    void testExtractFromMapAndList() {
        Map<Object, Object> testMap = getTestMap();
        testMap.put("f4", Collections.singletonList(Collections.singletonMap("f5", "v5")));

        assertEquals("v5", valueExtractor.extract(Collections.singletonList(testMap), "[0].f4.[0].f5"));
    }

    @Test
    @DisplayName("Extract from List last String value")
    void testExtractFromList() {
        assertEquals("v1", valueExtractor.extract(Arrays.asList("v1", "v2"), "[0]"));
    }

    @Test
    @DisplayName("Extract size of List")
    void testExtractSizeOfList() {
        ArrayList<Object> objects = new ArrayList<>();
        objects.add("v1");
        objects.add("v2");

        assertEquals(2, valueExtractor.extract(objects, "size"));
    }

    @Test
    @DisplayName("Extract size of Map")
    void testExtractLengthOfArray() {
        assertEquals(1, valueExtractor.extract(getTestMap(), "f1.size"));
    }

    @Test
    @DisplayName("Extract from Array last String value")
    void testExtractFromArray() {
        assertEquals("v1", valueExtractor.extract(new String[]{"v1", "v2"}, "[0]"));
    }

    @Test
    @DisplayName("Extract from List of List last String value")
    void testExtractFromListOfList() {
        assertEquals("v2", valueExtractor.extract(Collections.singletonList(Arrays.asList("v1", "v2")), "[0].[1]"));
    }

    @Test
    @DisplayName("Extract from object of a class")
    void testExtractFromCustomObject() {
        TestObject testObject = new TestObject("test");

        assertEquals(testObject.getName(), valueExtractor.extract(testObject, "name"));
        assertEquals(testObject.getName().length(), valueExtractor.extract(testObject, "name.length"));
    }

    @Test
    @DisplayName("Extract from object of a class with List and Map inside")
    void testExtractFromCustomObjectWithListAndMap() {
        List<Map<String, Object>> object =
                Collections.singletonList(Collections.singletonMap("f1",
                        new ComplexTestObject(Collections.singletonMap("f2", new TestObject("test")))));

        assertEquals("test", valueExtractor.extract(object, "[0].f1.params.f2.name"));
    }

    @Test
    @DisplayName("Extract from object where method not found")
    void testExtractFromObjectWhenMethodNotFound() {
        assertThrows(ValueExtractionException.class, () -> valueExtractor.extract(new Object(), "param"));
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

    private class TestObject {
        private String name;

        public TestObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private class ComplexTestObject {
        private Map<String, Object> params;

        public ComplexTestObject(Map<String, Object> params) {
            this.params = params;
        }

        public Map<String, Object> getParams() {
            return params;
        }
    }
}