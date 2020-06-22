package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;

@DisplayName("Convert test data from ftl")
class TemplateTestDataConverterTest {

    private TemplateTestDataConverter templateTestDataConverter;

    @BeforeEach
    void setUp() {
        ClassLoader classLoader = getClass().getClassLoader();
        templateTestDataConverter =
                new TemplateTestDataConverter(classLoader, new CsvTestDataConverter(classLoader));
    }

    @Test
    @DisplayName("When file and simple data model provided")
    void testFtlConversion() {
        Map<String, Object> templateDetails =
                createMap("template",
                        createMap(
                                "file", "template/test.ftl",
                                "name", "n1",
                                "value", "v1"
                        ));

        Object result = templateTestDataConverter.convert(templateDetails);

        assertAll(
                () -> assertEquals(
                        "{\n" +
                                " \"name\" : \"n1\"\n" +
                                " \"value\" : \"v1\"\n" +
                                "}",
                        result
                )
        );
    }

    @Test
    @DisplayName("When file and csv data model provided with row filter Then check correct row applied")
    void testFtlConversionWithCsvDataModel() {
        Map<String, Object> templateDetails =
                createMap("template",
                        createMap(
                                "file", "template/test.ftl",
                                "csv", createMap(
                                        "file", "csv/test-file.csv",
                                        "row", createMap("id", "3")
                                )
                        ));

        Object result = templateTestDataConverter.convert(templateDetails);

        assertAll(
                () -> assertEquals(
                        "{\n" +
                                " \"name\" : \"n3\"\n" +
                                " \"value\" : \"v3\"\n" +
                                "}",
                        result
                )
        );
    }

    @Test
    @DisplayName("When file and csv data model provided without filter Then check first row from csv applied")
    void testFtlConversionWithCsvDataModelWithoutFilter() {
        Map<String, Object> templateDetails =
                createMap("template",
                        createMap(
                                "file", "template/test.ftl",
                                "csv", createMap("file", "csv/test-file.csv")
                        ));

        Object result = templateTestDataConverter.convert(templateDetails);

        assertAll(
                () -> assertEquals(
                        "{\n" +
                                " \"name\" : \"n1\"\n" +
                                " \"value\" : \"v1\"\n" +
                                "}",
                        result
                )
        );
    }

    @Test
    @DisplayName("When file and csv data model provided without filter Then check first row from csv applied")
    void testFtlConversionWhenFileNotExist() {
        Map<String, Object> templateDetails =
                createMap("template",
                        createMap(
                                "file", "template/invalid.ftl"
                        ));

        assertThrows(TestDataConversionException.class, () -> templateTestDataConverter.convert(templateDetails));
    }
}