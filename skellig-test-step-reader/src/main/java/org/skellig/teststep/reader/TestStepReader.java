package org.skellig.teststep.reader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public interface TestStepReader {

    List<Map<String, Object>> read(Path fileName);

}
