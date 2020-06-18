package org.skellig.teststep.processing.runner;

import org.skellig.teststep.processing.exception.TestStepRegistryException;
import org.skellig.teststep.processing.model.TestStepFileExtension;
import org.skellig.teststep.reader.TestStepReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class TestStepsRegistry {

    private Collection<Map<String, Object>> testSteps;
    private TestStepFileExtension testStepFileExtension;
    private Map<String, Pattern> stepNamePatternsCache;
    private TestStepReader testStepReader;
    private Collection<Path> testStepsPaths;

    TestStepsRegistry(TestStepFileExtension testStepFileExtension,
                      TestStepReader testStepReader) {
        this.testStepFileExtension = testStepFileExtension;
        this.testStepReader = testStepReader;
        stepNamePatternsCache = new ConcurrentHashMap<>();
    }

    void registerFoundTestStepsInPath(Collection<Path> testStepsPaths) {
        this.testStepsPaths = testStepsPaths;
        testSteps = getTestStepsFromPath(testStepsPaths);
    }

    Optional<Map<String, Object>> getByName(String testStepName) {
        return testSteps.parallelStream()
                .filter(testStep -> getPatternOfTestStep(getTestStepName(testStep)).matcher(testStepName).matches())
                .findFirst();
    }

    Map<String, String> extractParametersFromTestStepName(Map<String, Object> rawTestStep, String testStepName) {
        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = getPatternOfTestStep(getTestStepName(rawTestStep)).matcher(testStepName);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                parameters.put("$" + i, matcher.group(i));
            }
        }
        return parameters;
    }

    private String getTestStepName(Map<String, Object> rawTestStep) {
        return String.valueOf(rawTestStep.get("name"));
    }

    Collection<Path> getTestStepsRootPath() {
        return testStepsPaths;
    }

    private Pattern getPatternOfTestStep(String testStepName) {
        return stepNamePatternsCache.computeIfAbsent(testStepName, v -> Pattern.compile(testStepName));
    }

    private Collection<Map<String, Object>> getTestStepsFromPath(Collection<Path> rootPaths) {
        return rootPaths.stream()
                .map(rootPath -> {
                    try {
                        return Files.walk(rootPath)
                                .parallel()
                                .filter(path -> String.valueOf(path.getFileName()).endsWith(testStepFileExtension.getName()))
                                .map(filePath -> testStepReader.read(filePath))
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());
                    } catch (IOException e) {
                        throw new TestStepRegistryException(e.getMessage(), e);
                    }
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}