package org.skellig.teststep.runner;

import org.skellig.teststep.processing.utils.CachedPattern;
import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.runner.exception.TestStepRegistryException;
import org.skellig.teststep.runner.model.TestStepFileExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class TestStepsRegistry {

    private Collection<Map<String, Object>> testSteps;
    private TestStepFileExtension testStepFileExtension;
    private TestStepReader testStepReader;
    private Collection<Path> testStepsPaths;

    TestStepsRegistry(TestStepFileExtension testStepFileExtension,
                      TestStepReader testStepReader) {
        this.testStepFileExtension = testStepFileExtension;
        this.testStepReader = testStepReader;
    }

    void registerFoundTestStepsInPath(Collection<Path> testStepsPaths) {
        this.testStepsPaths = testStepsPaths;
        testSteps = getTestStepsFromPath(testStepsPaths);
    }

    Optional<Map<String, Object>> getByName(String testStepName) {
        return testSteps.parallelStream()
                .filter(testStep -> CachedPattern.compile(getTestStepName(testStep)).matcher(testStepName).matches())
                .findFirst();
    }

    private String getTestStepName(Map<String, Object> rawTestStep) {
        return String.valueOf(rawTestStep.get("name"));
    }

    Collection<Path> getTestStepsRootPath() {
        return testStepsPaths;
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