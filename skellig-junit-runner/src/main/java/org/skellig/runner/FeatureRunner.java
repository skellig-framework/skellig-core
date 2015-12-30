package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.skellig.feature.Feature;
import org.skellig.feature.TestScenario;
import org.skellig.feature.TestStep;
import org.skellig.runner.exception.FeatureRunnerException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FeatureRunner extends ParentRunner<TestScenarioRunner> {

    private List<TestScenarioRunner> testScenarioRunners;
    private Feature feature;
    private Description description;

    public FeatureRunner(Feature feature) throws InitializationError {
        super(feature.getClass());
        testScenarioRunners = feature.getScenarios().stream()
                .map(TestScenarioRunner::create)
                .collect(Collectors.toList());
        this.feature = feature;
    }

    @Override
    public Description getDescription() {
        if (description == null) {
            description = Description.createSuiteDescription(getName(), feature.getName());
            getChildren().forEach(child -> description.addChild(describeChild(child)));
        }
        return description;
    }

    @Override
    protected String getName() {
        return feature.getName();
    }

    @Override
    protected List<TestScenarioRunner> getChildren() {
        return testScenarioRunners;
    }

    @Override
    protected Description describeChild(TestScenarioRunner child) {
        return child.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected void runChild(TestScenarioRunner child, RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(child));
        try {
            child.run(notifier);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(describeChild(child), e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(describeChild(child));
        }
    }

    public static FeatureRunner create(Feature feature) {
        try {
            return new FeatureRunner(feature);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }
}

class TestScenarioRunner extends ParentRunner<TestStep> {

    private final Map<TestStep, Description> stepDescriptions = new HashMap<>();
    private Description description;
    private TestScenario testScenario;

    private TestScenarioRunner(TestScenario testScenario) throws InitializationError {
        super(testScenario.getClass());
        this.testScenario = testScenario;
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
        if (description == null) {
            description = Description.createSuiteDescription(getName(), testScenario.getName());
            getChildren().forEach(step -> description.addChild(describeChild(step)));
        }
        return description;
    }

    @Override
    public Description describeChild(TestStep step) {
        Description description = stepDescriptions.get(step);
        if (description == null) {
            description = Description.createTestDescription(getName(), step.getName(), step.getName());
            stepDescriptions.put(step, description);
        }
        return description;
    }

    @Override
    protected void runChild(TestStep child, RunNotifier notifier) {
        notifier.fireTestStarted(describeChild(child));
        try {
            //TODO: run test case from test data
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(describeChild(child), e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(describeChild(child));
        }
    }

    public static TestScenarioRunner create(TestScenario testScenario) {
        try {
            return new TestScenarioRunner(testScenario);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }
}