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
        private TestStepFactory testStepFactory;

        public Builder withTestStepFactory(TestStepFactory testStepFactory) {
            this.testStepFactory = testStepFactory;
            return this;
        }

        public Builder withDefaultTestStepFactory(Properties testStepKeywordProperties) {
            this.testStepFactory =
                    new DefaultTestStepFactory.Build()
                            .withDefaultFactories(testStepKeywordProperties)
                            .build();
            return this;
        }

        public TestStepReader build() {
            return new StsTestStepReader(testStepFactory);
        }
    }
}
