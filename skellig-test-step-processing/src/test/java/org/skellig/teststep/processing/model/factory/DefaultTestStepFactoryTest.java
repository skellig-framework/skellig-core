package org.skellig.teststep.processing.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.converter.DefaultValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.state.TestScenarioState;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultTestStepFactoryTest {

    private TestStepFactory testStepFactory;
    private TestScenarioState testScenarioState;

    @BeforeEach
    void setUp() {
        testScenarioState = mock(TestScenarioState.class);
        testStepFactory = new DefaultTestStepFactory.Builder()
                .withTestStepValueConverter(
                        new DefaultValueConverter.Builder()
                                .withTestScenarioState(testScenarioState)
                                .build()
                )
                .build();
    }

    @Test
    void testCreateTestStepWithVariablesAndAppliedParameters() {
        String generatedId = "0001";
        when(testScenarioState.get("gen_id")).thenReturn(Optional.of(generatedId));

        Map<String, Object> rawTestStep =
                createMap("variables",
                        createMap(
                                "id", "get(gen_id)",
                                "names", Arrays.asList("n1", "${name:n2}"),
                                "amount", "${amt:500}"
                        ),
                        "payload", "${names}");

        Map<String, String> parameters = Collections.singletonMap("amt", "100");

        TestStep testStep = testStepFactory.create("test 1", rawTestStep, parameters);

        assertAll(
                () -> assertTrue(((List)testStep.getTestData()).containsAll(Arrays.asList("n1", "n2"))),
                () -> assertEquals(generatedId, testStep.getVariables().get("id")),
                () -> assertTrue(((List)testStep.getVariables().get("names")).containsAll(Arrays.asList("n1", "n2"))),
                () -> assertEquals("100", testStep.getVariables().get("amount"))
        );
    }

    private Map<String, Object> createMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            map.put((String) params[i], params[i + 1]);
        }
        return map;
    }
}