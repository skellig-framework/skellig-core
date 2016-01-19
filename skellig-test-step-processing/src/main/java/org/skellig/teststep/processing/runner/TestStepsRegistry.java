package org.skellig.teststep.processing.runner;

import org.skellig.teststep.processing.exception.TestStepRegistryException;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.TestStepFileExtension;

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

    private Collection<TestStep> testSteps;
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
        try {
            testSteps = getTestStepsFromPath(testStepsPaths);
        } catch (IOException e) {
            throw new TestStepRegistryException(e.getMessage(), e);
        }
    }

    Optional<TestStep> getByName(String testStepName) {
        return testSteps.parallelStream()
                .filter(testStep -> getPatternOfTestStep(testStep.getName()).matcher(testStepName).matches())
                .findFirst();
    }

    Map<String, String> extractParametersFromTestStepName(TestStep testStep, String testStepName) {
        Map<String, String> parameters = new HashMap<>();
        Matcher matcher = getPatternOfTestStep(testStep.getName()).matcher(testStepName);
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                parameters.put("$" + i, matcher.group(i));
            }
        }
        return parameters;
    }

    Collection<Path> getTestStepsRootPath() {
        return testStepsPaths;
    }

    private Pattern getPatternOfTestStep(String testStepName) {
        return stepNamePatternsCache.computeIfAbsent(testStepName, v -> Pattern.compile(testStepName));
    }

    private Collection<TestStep> getTestStepsFromPath(Collection<Path> rootPaths) throws IOException {
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