package org.skellig.teststep.processing.model;

public enum TestStepExecutionType {
    SYNC("sync"),
    ASYNC("async");

    private String name;

    TestStepExecutionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static TestStepExecutionType fromName(String name) {
        for (TestStepExecutionType value : values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        return TestStepExecutionType.SYNC;
    }
}
