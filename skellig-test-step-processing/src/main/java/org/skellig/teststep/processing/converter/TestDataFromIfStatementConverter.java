package org.skellig.teststep.processing.converter;

import org.apache.commons.lang3.math.NumberUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Map;
import java.util.Objects;

class TestDataFromIfStatementConverter implements TestDataConverter {

    private static final String IF_KEYWORD = "if";
    private static final String CONDITION_KEYWORD = "condition";
    private static final String THEN_KEYWORD = "then";
    private static final String ELSE_KEYWORD = "else";

    private ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

    public Object convert(Object value) {
        if (value instanceof Map) {
            Map<String, Object> valueAsMap = (Map<String, Object>) value;
            if (valueAsMap.containsKey(IF_KEYWORD)) {
                Map<String, Object> ifDetails = (Map<String, Object>) valueAsMap.get(IF_KEYWORD);
                String condition = (String) ifDetails.get(CONDITION_KEYWORD);
                Object thenContent = ifDetails.get(THEN_KEYWORD);
                Object elseContent = ifDetails.getOrDefault(ELSE_KEYWORD, "");

                Objects.requireNonNull(condition, "'condition' is mandatory in 'if' statement");
                Objects.requireNonNull(thenContent, "'then' is mandatory in 'if' statement");

                return isConditionSatisfied(condition) ? thenContent : elseContent;
            }
        }
        return value;
    }

    private boolean isConditionSatisfied(String condition) {
        StringBuilder newCondition = encloseStringWithQuotes(condition);

        try {
            return (Boolean) engine.eval(newCondition.toString());
        } catch (Exception e) {
            return false;
        }
    }

    private StringBuilder encloseStringWithQuotes(String condition) {
        StringBuilder newCondition = new StringBuilder();

        for (int i = 0; i < condition.length(); i++) {
            char c = condition.charAt(i);
            if (isSpecialSymbol(c)) {
                newCondition.append(c);
            } else {
                String value = "";
                for (; i < condition.length(); i++) {
                    c = condition.charAt(i);
                    if (!isSpecialSymbol(c)) {
                        value += c;
                    } else {
                        i--;
                        break;
                    }
                }
                value = value.trim();
                newCondition.append(value.equals("") || NumberUtils.isNumber(value) ? value : "'" + value + "'");
            }
        }
        return newCondition;
    }

    private boolean isSpecialSymbol(char c) {
        return c == '(' || c == ')' || c == '<' || c == '=' || c == '>' || c == '&' || c == '|';
    }
}