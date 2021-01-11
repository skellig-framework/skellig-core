package org.skellig.teststep.processor.cassandra;

import com.typesafe.config.Config;
import org.skellig.teststep.processing.converter.TestStepResultConverter;
import org.skellig.teststep.processing.processor.TestStepProcessor;
import org.skellig.teststep.processing.state.TestScenarioState;
import org.skellig.teststep.processing.validation.TestStepResultValidator;
import org.skellig.teststep.processor.cassandra.model.CassandraDetails;
import org.skellig.teststep.processor.db.DatabaseTestStepProcessor;
import org.skellig.teststep.processor.db.model.DatabaseTestStep;

import java.util.Map;

class CassandraTestStepProcessor extends DatabaseTestStepProcessor<CassandraRequestExecutor> {

    protected CassandraTestStepProcessor(Map<String, CassandraRequestExecutor> dbServers,
                                         TestScenarioState testScenarioState,
                                         TestStepResultValidator validator,
                                         TestStepResultConverter testStepResultConverter) {
        super(dbServers, testScenarioState, validator, testStepResultConverter);
    }

    public static class Builder extends DatabaseTestStepProcessor.Builder<CassandraDetails, CassandraRequestExecutor> {

        private CassandraDetailsConfigReader cassandraDetailsConfigReader;

        public Builder() {
            cassandraDetailsConfigReader = new CassandraDetailsConfigReader();
        }

        @Override
        public DatabaseTestStepProcessor.Builder<CassandraDetails, CassandraRequestExecutor> withDbServers(Config config) {
            cassandraDetailsConfigReader.read(config).forEach(this::withDbServer);
            return this;
        }

        @Override
        protected CassandraRequestExecutor createRequestExecutor(CassandraDetails databaseDetails) {
            return new CassandraRequestExecutor(databaseDetails);
        }

        @Override
        public TestStepProcessor<DatabaseTestStep> build() {
            return new CassandraTestStepProcessor(dbServers, testScenarioState, validator, testStepResultConverter);
        }
    }
}
