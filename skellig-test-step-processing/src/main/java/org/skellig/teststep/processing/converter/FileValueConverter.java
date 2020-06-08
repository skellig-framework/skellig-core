package org.skellig.teststep.processing.converter;


import org.skellig.teststep.processing.exception.TestStepProcessingException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileValueConverter implements TestStepValueConverter {

    private static final String FILE_REGEX = "file\\((.+)\\)";

    private final Pattern filePathPattern;
    private final ClassLoader classLoader;

    public FileValueConverter(ClassLoader classLoader) {
        this.classLoader = classLoader;
        filePathPattern = Pattern.compile(FILE_REGEX);
    }

    @Override
    public Object convert(String value) {
        Matcher matcher = filePathPattern.matcher(value);
        if (matcher.find()) {
            return readFileContentFromFilePath(matcher.group(1));
        }
        return value;
    }

    private String readFileContentFromFilePath(String pathToFile) {
        URL resource = classLoader.getResource(pathToFile);
        if (resource != null) {
            try {
                return new String(Files.readAllBytes(Paths.get(resource.toURI())), StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new TestStepProcessingException(String.format("Failed to read file '%s'", pathToFile), e);
            }
        } else {
            throw new TestStepProcessingException(String.format("File '%s' doesn't exist", pathToFile));
        }
    }
}