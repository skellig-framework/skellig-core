package org.skellig.teststep.reader;

import org.skellig.teststep.reader.model.TestStep;

import java.nio.file.Path;
import java.util.List;

public interface TestStepReader {

    List<TestStep> read(Path fileName);

}
