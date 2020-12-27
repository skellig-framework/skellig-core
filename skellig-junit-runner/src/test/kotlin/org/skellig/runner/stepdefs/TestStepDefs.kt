package org.skellig.runner.stepdefs;

import org.skellig.teststep.runner.annotation.TestStep;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestStepDefs {

    @TestStep(name = "Log (.*)")
    public void logResult(String value, Map<String, String> parameters) {
        assertNotNull(value);
        assertEquals(1, parameters.size());
        System.out.println(value);
    }
}
