package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.exception.TestValueConversionException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Convert to file content")
class FileValueConverterTest {

    private FileValueConverter fileValueConverter;

    @BeforeEach
    void setUp() {
        fileValueConverter = new FileValueConverter(getClass().getClassLoader());
    }

    @Test
    @DisplayName("When file exist Then check content is returned")
    void testValidatePathToJsonFile() {
        String filePath = "csv/test-file.csv";
        String expectedContent = readFromFileExpectedResult("/" + filePath);

        assertEquals(expectedContent, fileValueConverter.convert(String.format("file(%s)", filePath)));
    }

    @Test
    @DisplayName("When file doesn't exist Then throw exception")
    void testFilePathIsEmpty() {
        String filePath = "file(invalid)";

        TestValueConversionException exception =
                assertThrows(TestValueConversionException.class, () -> fileValueConverter.convert(filePath));

        assertEquals("File 'invalid' doesn't exist", exception.getMessage());
    }

    private String readFromFileExpectedResult(String pathToFile) {
        try {
            Path path = Paths.get(getClass().getResource(pathToFile).toURI());
            return new String(Files.readAllBytes(path));
        } catch (Exception e) {
            return null;
        }
    }

}