package org.skellig.teststep.processing.utils;

import org.skellig.teststep.processing.model.ExpectedResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class UnitTestUtils {

    public static Map<String, Object> createMap(Object... params) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < params.length; i += 2) {
            map.put((String) params[i], params[i + 1]);
        }
        return map;
    }

    public static ExpectedResult extractExpectedValue(ExpectedResult expectedResult, int... indexPath) {
        for (int index : indexPath) {
            expectedResult = expectedResult.<List<ExpectedResult>>getExpectedResult().get(index);
        }
        return expectedResult;
    }
}
