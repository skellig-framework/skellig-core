package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;

@DisplayName("Convert csv test data")
class CsvTestDataConverterTest {

    private CsvTestDataConverter csvTestDataConverter;

    @BeforeEach
    void setUp() {
        csvTestDataConverter = new CsvTestDataConverter(getClass().getClassLoader());
    }

    @Test
    @DisplayName("Without row filter Then check all raws read")
    void testConvertFromCsvFile() {
        Map<String, Object> csvDetails =
                createMap("csv",
                        createMap("file", "csv/test-file.csv"));

        Object result = csvTestDataConverter.convert(csvDetails);

        assertAll(
                () -> assertEquals("1", ((Map) ((List) result).get(0)).get("id")),
                () -> assertEquals("n1", ((Map) ((List) result).get(0)).get("name")),
                () -> assertEquals("v1", ((Map) ((List) result).get(0)).get("value")),

                () -> assertEquals("2", ((Map) ((List) result).get(1)).get("id")),
                () -> assertEquals("n2", ((Map) ((List) result).get(1)).get("name")),
                () -> assertEquals("v2", ((Map) ((List) result).get(1)).get("value")),

                () -> assertEquals("3", ((Map) ((List) result).get(2)).get("id")),
                () -> assertEquals("n3", ((Map) ((List) result).get(2)).get("name")),
                () -> assertEquals("v3", ((Map) ((List) result).get(2)).get("value"))
        );
    }

    @Test
    @DisplayName("With row filter Then check filtered raws read")
    void testConvertFromCsvFileWithFiltering() {
        Map<String, Object> csvDetails =
                createMap("csv",
                        createMap(
                                "file", "csv/test-file.csv",
                                "row",
                                createMap("id", "2", "name", "n2"))
                );

        Object result = csvTestDataConverter.convert(csvDetails);

        assertAll(
                () -> assertEquals(1, ((List) result).size()),

                () -> assertEquals("2", ((Map) ((List) result).get(0)).get("id")),
                () -> assertEquals("n2", ((Map) ((List) result).get(0)).get("name")),
                () -> assertEquals("v2", ((Map) ((List) result).get(0)).get("value"))
        );
    }

    @Test
    @DisplayName("When file does not exist Then throw exception")
    void testConvertWhenFileNotExist() {
        Map<String, Object> csvDetails =
                createMap("csv", createMap("file", "csv/missing.csv"));

        assertThrows(TestDataConversionException.class,
                () -> csvTestDataConverter.convert(csvDetails));
    }
}