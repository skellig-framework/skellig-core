package org.skellig.teststep.runner;

import org.skellig.teststep.runner.annotation.TestStep;
import org.skellig.teststep.runner.exception.TestStepRegistryException;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

class ClassTestStepsRegistry {

    private static final String CLASS_EXTENSION = ".class";

    private List<TestStepDefDetails> testStepsPerClass;

    public ClassTestStepsRegistry() {
        testStepsPerClass = new ArrayList<>();
    }

    Optional<TestStepDefDetails> getTestStep(String testStepName) {
        return testStepsPerClass.stream()
                .filter(entry -> entry.getTestStepNamePattern().matcher(testStepName).matches())
                .findFirst();
    }

    void registerFoundTestStepInClasses(Collection<String> packages, ClassLoader classLoader) {
        packages.forEach(resourcePath -> {
            URL resource = classLoader.getResource(resourcePath.replace('.', '/'));
            if (resource == null) {
                throw new TestStepRegistryException("No resources found in " + resourcePath);
            } else {
                try {
                    processDirectory(new File(resource.getPath()), resourcePath);
                } catch (Exception e) {
                    throw new TestStepRegistryException("Can't load the class", e);
                }
            }
        });
    }

    private void processDirectory(File file, String packageName) throws Exception {
        for (String fileName : Objects.requireNonNull(file.list())) {
            if (fileName.endsWith(CLASS_EXTENSION)) {
                String className = packageName + '.' + fileName.substring(0, fileName.length() - CLASS_EXTENSION.length());

                Class<?> foundClass = Class.forName(className);
                Object foundClassInstance = null;
                for (Method method : foundClass.getMethods()) {
                    if (method.isAnnotationPresent(TestStep.class)) {
                        TestStep testStepAnnotation = method.getAnnotation(TestStep.class);
                        Pattern testStepNamePattern = Pattern.compile(testStepAnnotation.name());
                        if (foundClassInstance == null) {
                            foundClassInstance = foundClass.newInstance();
                        }
                        testStepsPerClass.add(new TestStepDefDetails(testStepNamePattern, foundClassInstance, method));
                    }
                }
            } else {
                File subdir = new File(file, fileName);
                if (subdir.isDirectory()) {
                    processDirectory(subdir, packageName + '.' + fileName);
                }
            }
        }
    }

    static final class TestStepDefDetails {
        private Pattern testStepNamePattern;
        private Object testStepDefInstance;
        private Method testStepMethod;

        public TestStepDefDetails(Pattern testStepNamePattern, Object testStepDefInstance, Method testStepMethod) {
            this.testStepNamePattern = testStepNamePattern;
            this.testStepDefInstance = testStepDefInstance;
            this.testStepMethod = testStepMethod;
        }

        public Pattern getTestStepNamePattern() {
            return testStepNamePattern;
        }

        public Object getTestStepDefInstance() {
            return testStepDefInstance;
        }

        public Method getTestStepMethod() {
            return testStepMethod;
        }
    }
}
