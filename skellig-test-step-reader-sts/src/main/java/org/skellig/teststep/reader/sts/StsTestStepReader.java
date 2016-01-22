package org.skellig.teststep.reader.sts;

import org.skellig.teststep.reader.TestStepReader;
import org.skellig.teststep.reader.model.TestStep;
import org.skellig.teststep.reader.model.factory.DefaultTestStepFactory;
import org.skellig.teststep.reader.model.factory.TestStepFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class StsTestStepReader implements TestStepReader {

    private StsFileParser parser;
    private TestStepFactory testStepFactory;

    protected StsTestStepReader(TestStepFactory testStepFactory) {
        this.parser = new StsFileParser();
        this.testStepFactory = testStepFactory;
    }

    @Override
    public List<TestStep> read(Path fileName) {
        return parser.parse(fileName).stream()
                .map(rawTestStep -> testStepFactory.create(rawTestStep))
                .collect(Collectors.toList());
    }

    public static class Builder {

        private DefaultTestStepFactory.Builder testStepFactoryBuilder;

        public Builder() {
            testStepFactoryBuilder = new DefaultTestStepFactory.Builder();
        }

        public Builder withTestStepFactory(TestStepFactory testStepFactory) {
            testStepFactoryBuilder.withTestStepFactory(testStepFactory);
            return this;
        }

        public TestStepReader build(Properties testStepKeywordProperties) {

            return new StsTestStepReader(testStepFactoryBuilder
                    .withDefaultFactories(testStepKeywordProperties)
                    .build());
        }

        public TestStepReader build() {
            return build(null);
        }
    }
}
