package org.skellig.teststep.processing.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skellig.teststep.processing.utils.UnitTestUtils.createMap;

class TestDataFromIfStatementConverterTest {

    private TestDataFromIfStatementConverter converter;

    @BeforeEach
    void setUp() {
        converter = new TestDataFromIfStatementConverter();
    }

    @Test
    void testSimpleConditionWhenFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a 1 == b 1",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testSimpleConditionWhenTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a 1 == a 1",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testTwoConditionsWhenOneOfThemTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == b || c == c",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testTwoConditionsWhenNotAllAreTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == b && c == c",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testTwoConditionsWhenAllAreTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == a && cd d == cd d",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenMore() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "12 > 5",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenMoreAndFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "10 > 32.5",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenLess() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "12 < 5",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenLessAndTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "0.5 < 2.1",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenMoreOrEqual() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "5 >= 5",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenLessAndFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "3 >= 5",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenLessOrEqual() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "8 <= 7",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testSimpleConditionWithNumbersWhenLessOrEqualAndTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "123 <= 999.9",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testComplexConditionWithoutGroups() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == c || a == a && c == d || c == c",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testComplexConditionWithoutGroupsAndFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "abc == g || 56 <= 32 || b == n",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testComplexConditionWithGroupsAndFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "(a == c || a == a) && (c == d || d == c)",
                        "then", "true", "else", "false"));

        assertEquals("false", converter.convert(data));
    }

    @Test
    void testComplexConditionWithGroupsAndTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "(a == c && a == a) || (c == d || c == c)",
                        "then", "true", "else", "false"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testComplexCondition() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "success == success && (7 <= 12 || 7 == 100)",
                        "then", "a", "else", "b"));

        assertEquals("a", converter.convert(data));
    }

    @Test
    void testConditionWithThenOnlyAndTrue() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == a",
                        "then", "true"));

        assertEquals("true", converter.convert(data));
    }

    @Test
    void testConditionWithThenOnlyAndFalse() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == b", "then", "true"));

        assertEquals("", converter.convert(data));
    }

    @Test
    void testConditionWhenThenNotProvided() {
        Map<String, Object> data = createMap("if",
                createMap("condition", "a == a"));

        NullPointerException ex = assertThrows(NullPointerException.class, () -> converter.convert(data));

        assertEquals("'then' is mandatory in 'if' statement", ex.getMessage());
    }


    @Test
    void testWhenConditionNotProvided() {
        Map<String, Object> data = createMap("if",
                createMap("then", "a"));

        NullPointerException ex = assertThrows(NullPointerException.class, () -> converter.convert(data));

        assertEquals("'condition' is mandatory in 'if' statement", ex.getMessage());
    }
}