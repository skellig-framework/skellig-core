package org.skellig.teststep.processing.converter;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import org.skellig.teststep.processing.exception.TestDataConversionException;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

class CsvTestDataConverter implements TestDataConverter {

    private static final String ROW_KEYWORD = "row";
    private static final String CSV_KEYWORD = "csv";

    private ClassLoader classLoader;

    CsvTestDataConverter(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object convert(Object value) {
        if (value instanceof Map) {
            Map<String, Object> valueAsMap = (Map<String, Object>) value;
            if (valueAsMap.containsKey("csv")) {
                Map<String, Object> csv = (Map<String, Object>) valueAsMap.get(CSV_KEYWORD);
                String csvFile = (String) csv.get("file");
                Predicate<Map<String, String>> rowFilter;
                if (csv.containsKey("row")) {
                    Map<String, Object> row = (Map<String, Object>) valueAsMap.get(ROW_KEYWORD);
                    rowFilter = (item) ->
                            row.entrySet().stream()
                                    .allMatch(entry ->
                                            item.containsKey(entry.getKey()) && item.get(entry.getKey()).equals(entry.getValue()));
                } else {
                    rowFilter = (row) -> true;
                }

                value = read(csvFile, rowFilter);
            }
        }
        return value;
    }

    private List<Map<String, String>> read(String fileName, Predicate<Map<String, String>> rowFilter) {
        List<Map<String, String>> result = new ArrayList<>();

        Path pathToFile = getPathToFile(fileName);
        if (Files.exists(pathToFile)) {
            readTableFromCsv(pathToFile)
                    .ifPresent(csvContainer -> {
                        csvContainer.getRows()
                                .forEach(csvRow -> {
                                    Map<String, String> row = csvRow.getFieldMap();
                                    if (rowFilter.test(row)) {
                                        result.add(row);
                                    }
                                });
                    });
        } else {
            throw new RuntimeException(String.format("File %s does not exist", fileName));
        }
        return result;
    }

    private Path getPathToFile(String fileName) {
        URL url = classLoader.getResource(fileName);
        if (url == null) {
            throw new TestDataConversionException(String.format("File '%s' was not found in resources", fileName));
        } else {
            try {
                return Paths.get(url.toURI());
            } catch (URISyntaxException e) {
                throw new TestDataConversionException(String.format("Failed to get path to '%s'", fileName), e);
            }
        }
    }

    private Optional<CsvContainer> readTableFromCsv(Path pathToFile) {
        try {
            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);
            return Optional.ofNullable(csvReader.read(pathToFile, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}