package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.skellig.feature.TestScenario;
import org.skellig.feature.TestStep;
import org.skellig.runner.exception.FeatureRunnerException;
import org.skellig.test.processing.runner.TestStepRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestScenarioRunner extends ParentRunner<TestStep> {

    private final Map<Object, Description> stepDescriptions = new HashMap<>();
    private TestScenario testScenario;
    private TestStepRunner testStepRunner;

    protected TestScenarioRunner(TestScenario testScenario, TestStepRunner testStepRunner) throws InitializationError {
        super(testScenario.getClass());
        this.testScenario = testScenario;
        this.testStepRunner = testStepRunner;
    }

    @Override
    protected List<TestStep> getChildren() {
        return testScenario.getSteps();
    }

    @Override
    protected String getName() {
        return testScenario.getName();
    }

    @Override
    public Description getDescription() {
        return stepDescriptions.computeIfAbsent(this,
                o -> {
                    Description description = Description.createSuiteDescription(getName(), testScenario.getName());
                    getChildren().forEach(step -> description.addChild(describeChild(step)));
                    return description;
                });
    }

    @Override
    public Description describeChild(TestStep step) {
        return stepDescriptions.getOrDefault(step,
                Description.createTestDescription(getName(), step.getName(), step.getName()));
    }

    @Override
    protected void runChild(TestStep child, RunNotifier notifier) {
        Description childDescription = describeChild(child);
        notifier.fireTestStarted(childDescription);
        try {
            testStepRunner.run(child.getName(), child.getParameters().orElse(Collections.emptyMap()));
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(childDescription, e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(childDescription);
        }
    }

    public static TestScenarioRunner create(TestScenario testScenario, TestStepRunner testStepRunner) {
        try {
            return new TestScenarioRunner(testScenario, testStepRunner);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }
}