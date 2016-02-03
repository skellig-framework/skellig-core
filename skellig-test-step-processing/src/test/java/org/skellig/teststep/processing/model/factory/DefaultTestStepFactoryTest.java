package org.skellig.teststep.processing.model.factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.teststep.processing.converter.DefaultTestDataConverter;
import org.skellig.teststep.processing.converter.DefaultValueConverter;
import org.skellig.teststep.processing.model.TestStep;
import org.skellig.teststep.processing.model.ValidationDetails;
import org.skellig.teststep.processing.state.TestScenarioState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;
import static org.skellig.teststep.processing.utils.UnitTestUtils.extractExpectedValue;

@DisplayName("Create Test Step")
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
                .withTestDataConverter(
                        new DefaultTestDataConverter.Builder()
                                .withClassLoader(getClass().getClassLoader())
                                .build())
                .build();
    }

    @Test
    @DisplayName("With variables And parameters And payload has reference to variable")
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
                () -> assertTrue(((List) testStep.getTestData()).containsAll(Arrays.asList("n1", "n2"))),
                () -> assertEquals(generatedId, testStep.getVariables().get("id")),
                () -> assertTrue(((List) testStep.getVariables().get("names")).containsAll(Arrays.asList("n1", "n2"))),
                () -> assertEquals("100", testStep.getVariables().get("amount"))
        );
    }

    @Test
    @DisplayName("When payload is Map with simple fields")
    void testCreateTestStepWithPayloadAsMap() {

        String generatedId = "0001";
        when(testScenarioState.get("gen_id")).thenReturn(Optional.of(generatedId));

        Map<String, Object> rawTestStep =
                createMap(
                        "variables",
                        createMap(
                                "id", generatedId,
                                "rows", Arrays.asList(
                                        createMap("c1", "v1", "c2", "v2"),
                                        createMap("c1", "v3", "c2", "v4")
                                )
                        ),
                        "payload",
                        createMap(
                                "id", "${id}",
                                "size", "2",
                                "rows", "${rows}"
                        )
                );

        final long start = System.currentTimeMillis();
        TestStep testStep = testStepFactory.create("test 1", rawTestStep, Collections.emptyMap());
        System.out.println(System.currentTimeMillis() - start);

        assertAll(
                () -> assertEquals(generatedId, ((Map) testStep.getTestData()).get("id")),
                () -> assertEquals("2", ((Map) testStep.getTestData()).get("size")),
                () -> assertEquals("v1", ((Map) ((List) ((Map) testStep.getTestData()).get("rows")).get(0)).get("c1")),
                () -> assertEquals("v2", ((Map) ((List) ((Map) testStep.getTestData()).get("rows")).get(0)).get("c2")),
                () -> assertEquals("v3", ((Map) ((List) ((Map) testStep.getTestData()).get("rows")).get(1)).get("c1")),
                () -> assertEquals("v4", ((Map) ((List) ((Map) testStep.getTestData()).get("rows")).get(1)).get("c2"))
        );
    }

    @Test
    @DisplayName("With variables And validation details has reference to variable")
    void testCreateTestStepWithVariablesAndValidationDetails() {
        String generatedId = "0001";
        when(testScenarioState.get("gen_id")).thenReturn(Optional.of(generatedId));

        Map<String, Object> rawTestStep =
                createMap("variables",
                        createMap(
                                "id", generatedId,
                                "row", createMap("c1", "v1")
                        ),
                        "validate",
                        createMap(
                                "id", "${id}",
                                "result", "${row}"
                        ));

        TestStep testStep = testStepFactory.create("test 1", rawTestStep, Collections.emptyMap());
        ValidationDetails validationDetails = testStep.getValidationDetails().get();

        assertAll(
                () -> assertEquals("result", extractExpectedValue(validationDetails.getExpectedResult(), 0).getProperty()),
                () -> assertEquals("c1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getProperty()),
                () -> assertEquals("v1", extractExpectedValue(validationDetails.getExpectedResult(), 0, 0).getExpectedResult()),
                () -> assertEquals("id", extractExpectedValue(validationDetails.getExpectedResult(), 1).getProperty()),
                () -> assertEquals(generatedId, extractExpectedValue(validationDetails.getExpectedResult(), 1).getExpectedResult())
        );
    }

}