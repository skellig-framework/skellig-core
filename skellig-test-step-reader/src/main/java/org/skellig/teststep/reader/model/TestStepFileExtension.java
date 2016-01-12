package org.skellig.teststep.reader.model;

public enum TestStepFileExtension {
    STS("sts");

    private String extensionName;

    TestStepFileExtension(String extensionName) {
        this.extensionName = extensionName;
    }

    public String getName() {
        return extensionName;
    }
}
