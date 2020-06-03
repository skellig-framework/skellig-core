package org.skellig.teststep.reader.sts;

import org.skellig.teststep.reader.exception.TestStepReadException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StsFileParser {

    public List<Map<String, Object>> parse(Path filePath) {
        List<Map<String, Object>> rawTestSteps = new ArrayList<>();

        try (StsFileBufferedReader reader = new StsFileBufferedReader(Files.newBufferedReader(filePath));
             RawTestStepHandler rawTestStepHandler = new RawTestStepHandler()) {
            int character;
            while ((character = reader.read()) > 0) {
                rawTestStepHandler.handle(character, reader, rawTestSteps);
            }
            return rawTestSteps;
        } catch (Exception e) {
            throw new TestStepReadException(e);
        }
    }
}
