package org.skellig.teststep.reader;

import org.skellig.teststep.reader.model.TestStep;

import java.util.List;
import java.util.Map;

public interface TestStepReader {

    List<TestStep> read(String fileName);

    List<TestStep> read(String fileName, Map<String, String> parameters);
}
