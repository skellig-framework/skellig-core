package org.skellig.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.Statement;
import org.skellig.feature.parser.DefaultFeatureParser;
import org.skellig.runner.exception.FeatureRunnerException;
import org.skellig.test.processing.runner.DefaultTestStepRunner;
import org.skellig.test.processing.runner.TestStepRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SkelligRunner extends ParentRunner<FeatureRunner> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SkelligRunner.class);

    private List<FeatureRunner> children;

    public SkelligRunner(Class clazz) throws Exception {
        super(clazz);
        SkelligOptions skelligOptions = (SkelligOptions) clazz.getDeclaredAnnotation(SkelligOptions.class);

        List<Path> testStepPaths = Stream.of(skelligOptions.testSteps())
                .map(path -> {
                    try {
                        return Paths.get(clazz.getResource(path).toURI());
                    } catch (URISyntaxException e) {
                        throw new FeatureRunnerException(e.getMessage(), e);
                    }
                })
                .collect(Collectors.toList());

        TestStepRunner testStepRunner =
                new DefaultTestStepRunner.Builder()
                        .withTestStepReader(fileName -> null, testStepPaths)
                        .build();


        DefaultFeatureParser featureParser = new DefaultFeatureParser();
        Path pathToFeatures = Paths.get(getClass().getResource(skelligOptions.features()[0]).toURI());
        children = featureParser.parse(pathToFeatures.toString()).stream()
                .map(feature -> FeatureRunner.create(feature, testStepRunner))
                .collect(Collectors.toList());
    }

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
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


    class RunSkellig extends Statement {
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