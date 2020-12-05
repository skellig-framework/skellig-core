package org.skellig.teststep.processor.jdbc;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;
import org.skellig.teststep.processor.jdbc.model.JdbcDetails;

import java.util.Map;

public class JdbcTestStepProcessor extends DatabaseTestStepProcessor<JdbcRequestExecutor> {

    protected JdbcTestStepProcessor(Map<String, JdbcRequestExecutor> dbServers,
                                    TestScenarioState testScenarioState,
                                    TestStepResultValidator validator,
                                    TestStepResultConverter testStepResultConverter) {
        super(dbServers, testScenarioState, validator, testStepResultConverter);
    }

    public static class Builder extends DatabaseTestStepProcessor.Builder<JdbcDetails, JdbcRequestExecutor> {

        private JdbcDetailsConfigReader jdbcDetailsConfigReader;

        public Builder() {
            jdbcDetailsConfigReader = new JdbcDetailsConfigReader();
        }

        @Override
        public DatabaseTestStepProcessor.Builder<JdbcDetails, JdbcRequestExecutor> withDbServers(Config config) {
            jdbcDetailsConfigReader.read(config).forEach(this::withDbServer);
            return this;
        }

        @Override
        protected JdbcRequestExecutor createRequestExecutor(JdbcDetails databaseDetails) {
            return new JdbcRequestExecutor(databaseDetails);
        }

        @Override
        public TestStepProcessor<DatabaseTestStep> build() {
            return new JdbcTestStepProcessor(dbServers, testScenarioState, validator, testStepResultConverter);
        }
    }
}
