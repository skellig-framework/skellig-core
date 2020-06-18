package org.skellig.teststep.reader.sts;

import org.skellig.teststep.reader.TestStepReader;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class StsTestStepReader implements TestStepReader {

    private StsFileParser parser;

    public StsTestStepReader() {
        this.parser = new StsFileParser();
    }

    @Override
    public List<Map<String, Object>> read(Path fileName) {
        return parser.parse(fileName);
    }
}
