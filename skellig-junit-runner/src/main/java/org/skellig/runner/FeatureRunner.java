package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.skellig.feature.Feature;
import org.skellig.runner.exception.FeatureRunnerException;

import java.util.List;
import java.util.stream.Collectors;

public class FeatureRunner extends ParentRunner<TestScenarioRunner> {

    private Feature feature;
    private List<TestScenarioRunner> testScenarioRunners;
    private Description description;

    protected FeatureRunner(Feature feature) throws InitializationError {
        super(feature.getClass());
        this.feature = feature;
        testScenarioRunners =
                feature.getScenarios().stream()
                        .map(TestScenarioRunner::create)
                        .collect(Collectors.toList());
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
    protected void runChild(TestScenarioRunner child, RunNotifier notifier) {
        Description childDescription = describeChild(child);
        notifier.fireTestStarted(childDescription);
        try {
            child.run(notifier);
        } catch (Throwable e) {
            notifier.fireTestFailure(new Failure(childDescription, e));
            notifier.pleaseStop();
        } finally {
            notifier.fireTestFinished(childDescription);
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

