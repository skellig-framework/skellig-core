package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.Statement;
import org.skellig.feature.parser.DefaultFeatureParser;
import org.skellig.runner.annotation.SkelligOptions;
import org.skellig.runner.exception.FeatureRunnerException;
import org.skellig.runner.junit.report.ReportGenerator;
import org.skellig.runner.junit.report.SkelligReportGenerator;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.runner.TestStepRunner;
import org.skellig.teststep.runner.context.SkelligTestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkelligRunner extends ParentRunner<FeatureRunner> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkelligRunner.class);

    private List<FeatureRunner> children = new ArrayList<>();
    private ReportGenerator reportGenerator;
    private SkelligTestContext skelligTestContext;

    public SkelligRunner(Class clazz) throws Exception {
        super(clazz);
        SkelligOptions skelligOptions = (SkelligOptions) clazz.getDeclaredAnnotation(SkelligOptions.class);

        reportGenerator = new SkelligReportGenerator();

        skelligTestContext = skelligOptions.context().newInstance();
        TestStepRunner testStepRunner = skelligTestContext.initialize(clazz.getClassLoader(),
                Stream.of(skelligOptions.testSteps()).collect(Collectors.toList()));
        TestScenarioState testScenarioState = skelligTestContext.getTestScenarioState();

        DefaultFeatureParser featureParser = new DefaultFeatureParser();
        Stream.of(skelligOptions.features())
                .forEach(featureResourcePath -> {
                    try {
                        Path pathToFeatures = Paths.get(getClass().getClassLoader().getResource(featureResourcePath).toURI());
                        children.addAll(
                                featureParser.parse(pathToFeatures.toString()).stream()
                                        .map(feature -> FeatureRunner.create(feature, testStepRunner, testScenarioState))
                                        .collect(Collectors.toList())
                        );
                    } catch (URISyntaxException e) {
                        throw new FeatureRunnerException("Failed to read features from path: " + featureResourcePath, e);
                    }
                });
    }

    @Override
    public void run(RunNotifier notifier) {
        try {
            super.run(notifier);
        } finally {
            reportGenerator.generate(getChildren().stream().map(FeatureRunner::getFeatureReportDetails).collect(Collectors.toList()));
            skelligTestContext.cleanUp();
        }
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(FeatureRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(FeatureRunner child, RunNotifier notifier) {
        child.run(notifier);
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        Statement runFeatures = super.childrenInvoker(notifier);
        return new RunSkellig(runFeatures);
    }


    private static class RunSkellig extends Statement {
        private final Statement runFeatures;

        RunSkellig(Statement runFeatures) {
            this.runFeatures = runFeatures;
        }

        @Override
        public void evaluate() throws Throwable {
            runFeatures.evaluate();
        }
    }
}