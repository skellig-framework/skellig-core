package org.skellig.teststep.processing.converter;

import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class IncrementValueConverter implements TestStepValueConverter {

    private static final Pattern NAMED_INCREMENT_REGEX = Pattern.compile("inc\\((.*)\\)");
    private static final Pattern SPLIT_COMMA_REGEX = Pattern.compile(",");
    private static final Pattern SPLIT_SPACE_REGEX = Pattern.compile(" ");
    static final String DEFAULT_INC_NAME = "skellig_default";
    static final String FILE_NAME = "skellig-inc.tmp";

    @Override
    public Object convert(String value) {
        Matcher matcher = NAMED_INCREMENT_REGEX.matcher(value);
        String result = value;
        if (matcher.find()) {
            int maxLength = 1;
            String key = null;
            String[] params = SPLIT_COMMA_REGEX.split(matcher.group(1));
            if (params.length == 2) {
                key = params[0].trim();
                maxLength = Integer.parseInt(params[1].trim());
            } else if (params.length == 1) {
                String firstParameter = params[0].trim();
                if (isNumber(firstParameter)) {
                    maxLength = Integer.parseInt(firstParameter);
                } else if (firstParameter.length() > 0) {
                    key = firstParameter;
                } else {
                    key = DEFAULT_INC_NAME;
                }
            }

            Optional<String> currentValue = getCurrentValue(key, maxLength);
            result = incrementAndGet(currentValue.orElse(getDefaultValue(maxLength)),
                    maxLength == 1 && currentValue.isPresent() ? currentValue.get().length() : maxLength);
            replaceOldValueInFile(key, result, currentValue.isPresent());
        }
        return result;
    }

    private String incrementAndGet(String valueToIncrement, int maxLength) {
        int incrementedValue = Integer.parseInt(valueToIncrement) + 1;
        int newLength = String.valueOf(incrementedValue).length();
        return newLength <= maxLength ? StringUtils.repeat("0", maxLength - newLength) + incrementedValue : valueToIncrement;
    }

    private String getDefaultValue(int maxLength) {
        return StringUtils.repeat("0", maxLength);
    }

    private Optional<String> getCurrentValue(String key, int maxLength) {
        try {
            Path pathToFile = Paths.get(FILE_NAME);
            if (!Files.exists(pathToFile)) {
                Files.createFile(pathToFile);
            }

            try (Stream<String> lines = Files.lines(pathToFile)) {
                final Optional<String> first = lines.filter(line -> isMatchLine(key, maxLength, line))
                        .map(line -> key != null && line.startsWith(key) ? SPLIT_SPACE_REGEX.split(line)[1] : line)
                        .findFirst();
                return first;
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private boolean isMatchLine(String key, int maxLength, String line) {
        return (key != null && line.startsWith(key)) || (isNumber(line) && line.length() == maxLength);
    }

    private boolean isNumber(String line) {
        return line.length() > 0 && line.charAt(0) >= 48 && line.charAt(0) <= 57;
    }

    private void replaceOldValueInFile(String key, String newValue, boolean isOldValue) {
        try {
            Path pathToFile = Paths.get(FILE_NAME);
            if (Files.exists(pathToFile)) {
                if (isOldValue) {
                    try (Stream<String> lines = Files.lines(pathToFile)) {
                        List<String> newLines = lines
                                .map(line -> {
                                    if (isMatchLine(key, newValue.length(), line)) {
                                        return key != null && line.startsWith(key) ? key + " " + newValue : newValue;
                                    } else {
                                        return line;
                                    }
                                })
                                .collect(Collectors.toList());
                        Files.write(pathToFile, newLines);
                    }
                } else {
                    Files.write(pathToFile,
                            Collections.singletonList(key != null ? key + " " + newValue : newValue),
                            StandardOpenOption.APPEND);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        System.out.println("finalize");
    }

}