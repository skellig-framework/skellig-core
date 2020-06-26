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
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.reader.exception.ValidationException;
import org.skellig.teststep.runner.TestStepRunner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestScenarioRunner extends ParentRunner<TestStep> {

    private Map<Object, Description> stepDescriptions;
    private TestScenario testScenario;
    private TestStepRunner testStepRunner;
    private List<TestStepReportDetails> testStepsDataReport;
    private TestScenarioState testScenarioState;
    private boolean isChildFailed;

    protected TestScenarioRunner(TestScenario testScenario, TestStepRunner testStepRunner,
                                 TestScenarioState testScenarioState) throws InitializationError {
        super(testScenario.getClass());
        this.testScenario = testScenario;
        this.testStepRunner = testStepRunner;
        this.testScenarioState = testScenarioState;

        stepDescriptions = new HashMap<>();
        testStepsDataReport = new ArrayList<>();
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

        String testStepId = null;
        String errorLog = null;
        if (isChildFailed) {
            notifier.fireTestIgnored(childDescription);
        } else {
            notifier.fireTestStarted(childDescription);
            try {
                Map<String, String> parameters = child.getParameters().orElse(Collections.emptyMap());
                testStepId = testStepRunner.run(child.getName(), parameters);
            } catch (ValidationException ve) {
                fireFailureEvent(notifier, childDescription, ve);
                errorLog = ve.getMessage();
                testStepId = ve.getTestStepId();
            } catch (Throwable e) {
                fireFailureEvent(notifier, childDescription, e);
                errorLog = attachStackTrace(e);
            } finally {
                collectReportDetails(testStepId, child.getName(), errorLog);
                notifier.fireTestFinished(childDescription);
            }
        }
    }

    public TestScenarioReportDetails getTestScenarioReportDetails() {
        return new TestScenarioReportDetails(getName(), testStepsDataReport);
    }

    public static TestScenarioRunner create(TestScenario testScenario, TestStepRunner testStepRunner,
                                            TestScenarioState testScenarioState) {
        try {
            return new TestScenarioRunner(testScenario, testStepRunner, testScenarioState);
        } catch (InitializationError e) {
            throw new FeatureRunnerException(e.getMessage(), e);
        }
    }

    private void collectReportDetails(String testStepId, String testStepName, String errorLog) {
        TestStepReportDetails.Builder testStepReportBuilder =
                new TestStepReportDetails.Builder()
                        .withName(testStepName)
                        .withErrorLog(errorLog);
        testScenarioState.get(testStepId).ifPresent(testStepReportBuilder::withOriginalTestStep);
        testScenarioState.get(testStepId + ".result").ifPresent(testStepReportBuilder::withResult);
        testStepsDataReport.add(testStepReportBuilder.build());
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