package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.skellig.feature.TestScenario;
import org.skellig.feature.TestStep;
import org.skellig.runner.exception.FeatureRunnerException;
import org.skellig.runner.junit.report.model.TestScenarioReportDetails;
import org.skellig.runner.junit.report.model.TestStepReportDetails;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.runner.TestStepRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TestScenarioRunner extends ParentRunner<TestStep> {

    private Map<Object, Description> stepDescriptions;
    private TestScenario testScenario;
    private TestStepRunner testStepRunner;
    private List<TestStepReportDetails.Builder> testStepsDataReport;
    private List<TestStepProcessor.TestStepRunResult> testStepRunResults;
    private boolean isChildFailed;

    protected TestScenarioRunner(TestScenario testScenario, TestStepRunner testStepRunner) throws InitializationError {
        super(testScenario.getClass());
        this.testScenario = testScenario;
        this.testStepRunner = testStepRunner;

        stepDescriptions = new HashMap<>();
        testStepsDataReport = new ArrayList<>();
        testStepRunResults = new ArrayList<>();
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
    public void run(RunNotifier notifier) {
        try {
            super.run(notifier);
        } finally {
            try {
                // if there are any async test step running, then wait until they're finished
                // within set timeout. Cleanup results as they are no longer needed.
                testStepRunResults.forEach(TestStepProcessor.TestStepRunResult::awaitResult);
            } catch (Exception ex) {
                fireFailureEvent(notifier, getDescription(), ex);
            } finally {
                testStepRunResults = null;
            }
        }
    }

    @Override
    protected void runChild(TestStep child, RunNotifier notifier) {
        Description childDescription = describeChild(child);

        TestStepReportDetails.Builder testStepReportBuilder = new TestStepReportDetails.Builder().withName(child.getName());
        if (isChildFailed) {
            notifier.fireTestIgnored(childDescription);
            testStepsDataReport.add(testStepReportBuilder);
        } else {
            notifier.fireTestStarted(childDescription);
            try {
                Map<String, String> parameters = child.getParameters().orElse(Collections.emptyMap());
                TestStepProcessor.TestStepRunResult runResult = testStepRunner.run(child.getName(), parameters);
                testStepRunResults.add(runResult);

                // subscribe for result from test step. Usually needed for async test step
                // however if it's sync, then the function will be called anyway.
                runResult.subscribe((t, r, e) -> {
                    testStepReportBuilder.withOriginalTestStep(t).withResult(r);
                    if (e != null) {
                         /*
                           if test step is sync, then the thrown exception will be caught in runChild
                           if test step is async, then throwing exception doesn't give any effect, however
                           it will fail on 'run' method while waiting for the result and the error will be
                           registered in the report
                         */
                        testStepReportBuilder.withErrorLog(attachStackTrace(e));
                        throw e;
                    }
                });
            } catch (Throwable e) {
                fireFailureEvent(notifier, childDescription, e);
            } finally {
                testStepsDataReport.add(testStepReportBuilder);
                notifier.fireTestFinished(childDescription);
            }
        }
    }

    public TestScenarioReportDetails getTestScenarioReportDetails() {
        return new TestScenarioReportDetails(getName(),
                testStepsDataReport.stream()
                        .map(TestStepReportDetails.Builder::build)
                        .collect(Collectors.toList()));
    }

    public static TestScenarioRunner create(TestScenario testScenario, TestStepRunner testStepRunner) {
        try {
            return new TestScenarioRunner(testScenario, testStepRunner);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }

    private String attachStackTrace(Throwable e) {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter stackTraceWriter = new PrintWriter(stringWriter)) {
            stackTraceWriter.append("\n");
            e.printStackTrace(stackTraceWriter);
            return stringWriter.toString();
        } catch (Exception ioException) {
            return "";
        }
    }

    private void fireFailureEvent(RunNotifier notifier, Description childDescription, Throwable e) {
        notifier.fireTestFailure(new Failure(childDescription, e));
        isChildFailed = true;
    }
}