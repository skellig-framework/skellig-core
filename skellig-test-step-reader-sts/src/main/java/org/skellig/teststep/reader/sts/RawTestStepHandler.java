package org.skellig.teststep.reader.sts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RawTestStepHandler implements AutoCloseable {

    private int openedBrackets;
    private int bracketsNumber = 0;
    private String paramName;
    private boolean isEnclosedText;
    private boolean isParameter;
    private StringBuilder rawTestStepBuilder;

    RawTestStepHandler() {
        rawTestStepBuilder = new StringBuilder();
    }

    void handle(int character, StsFileBufferedReader reader, List<Map<String, Object>> rawTestSteps) throws IOException {
        if (character == '#') {
            handleCommentCharacter(reader);
        } else if (character == '(') {
            handleOpenParenthesis(character);
        } else if (character == ')') {
            handleClosedParenthesis(character, reader, rawTestSteps);
        } else {
            addCharacter(character);
        }
    }

    private Map<String, Object> readMap(StsFileBufferedReader reader, Map<String, Object> rawTestStep) throws IOException {
        rawTestStepBuilder.setLength(0);
        int character;
        while ((character = reader.read()) > 0) {
            if (character == '#') {
                handleCommentCharacter(reader);
            } else if (character == '\'') {
                handleSingleQuoteCharacter(rawTestStep);
            } else if (!isEnclosedText) {
                if (character == '}') {
                    if (handleClosedBracketCharacter(character, rawTestStep)) break;
                } else if (character == '{') {
                    handleOpenCurlyBracketCharacter(character, reader, rawTestStep);
                } else if (character == '=') {
                    handleEqualSignCharacter();
                } else if (character == '[' && rawTestStepBuilder.length() > 0) {
                    handleArrayBracketCharacter(reader, rawTestStep);
                } else if (character == '\n') {
                    handleNewLineCharacter(character, rawTestStep);
                } else {
                    addCharacter(character);
                }
            } else {
                addCharacter(character);
            }
        }
        return rawTestStep;
    }

    private List<Object> readList(StsFileBufferedReader reader) throws IOException {
        rawTestStepBuilder.setLength(0);
        int character;
        List<Object> result = new ArrayList<>();
        while ((character = reader.read()) > 0) {
            if (character == '#') {
                handleCommentCharacter(reader);
            } else if (character == '{') {
                handleListOpenedCurlyBracketCharacter(character, reader, result);
            } else if (character == '\n' && rawTestStepBuilder.length() > 0) {
                result.add(rawTestStepBuilder.toString());
                rawTestStepBuilder.setLength(0);
            } else if (character == ']') {
                break;
            } else {
                addCharacter((char) character);
            }

        }
        return result;
    }

    private void handleListOpenedCurlyBracketCharacter(int character, StsFileBufferedReader reader, List<Object> result) throws IOException {
        // '{' for list-type value means that its item will be Map
        if (rawTestStepBuilder.length() == 0 || isPreviousCharacterNotParameterSign()) {
            openedBrackets++;
            result.add(readMap(reader, new HashMap<>()));
            rawTestStepBuilder.setLength(0);
        } else {
            addCharacter(character);
        }
    }

    private boolean handleClosedBracketCharacter(int character, Map<String, Object> rawTestStep) {
        if (!isParameter) {
            // for '}' if paramName is not null then add its value
            openedBrackets--;
            if (paramName != null) {
                addParameterWithValue(rawTestStep);
            }
            return true;
        } else {
            // if '}' is part of parametrised value than close this parameter and include it in the future value
            isParameter = false;
            addCharacter(character);
        }
        return false;
    }

    private void handleNewLineCharacter(int character, Map<String, Object> rawTestStep) {
        // skip it if there is nothing to add to the builder
        if (rawTestStepBuilder.length() > 0) {
            // if paramName is read before '=' then we can add value to it.
            // Otherwise just add the character - usually when text is enclosed in single quotes
            if (paramName != null) {
                addParameterWithValue(rawTestStep);
            } else {
                addCharacter(character);
            }
        }
    }

    private void handleArrayBracketCharacter(StsFileBufferedReader reader, Map<String, Object> rawTestStep) throws IOException {
        rawTestStep.put(rawTestStepBuilder.toString(), readList(reader));
        rawTestStepBuilder.setLength(0);
    }

    private void handleEqualSignCharacter() {
        // after '=' sign we can set paramName and continue reading its value
        paramName = rawTestStepBuilder.toString();
        rawTestStepBuilder.setLength(0);
    }

    private void handleOpenCurlyBracketCharacter(int character, StsFileBufferedReader reader, Map<String, Object> rawTestStep) throws IOException {
        // if it's a parameter then continue reading.
        // Otherwise start read value as Map
        if (rawTestStepBuilder.length() > 0 && !isPreviousCharacterNotParameterSign()) {
            isParameter = true;
            addCharacter(character);
        } else {
            openedBrackets++;
            rawTestStep.put(rawTestStepBuilder.toString(), readMap(reader, new HashMap<>()));
            rawTestStepBuilder.setLength(0);
        }
    }

    private void handleSingleQuoteCharacter(Map<String, Object> rawTestStep) {
        // Single quote character means that we need to read the value till next single quote
        isEnclosedText = !isEnclosedText;
        if (!isEnclosedText && paramName != null) {
            addParameterWithValue(rawTestStep);
        }
    }

    private void handleCommentCharacter(StsFileBufferedReader reader) throws IOException {
        reader.readUntilFindCharacter('\n');
    }

    private void handleOpenParenthesis(int character) {
        // can be name of test step, or anything else, ex: regex, function, etc.
        if (bracketsNumber++ == 0) {
            paramName = rawTestStepBuilder.toString();
            rawTestStepBuilder.setLength(0);
        } else {
            addCharacter(character);
        }
    }

    private void handleClosedParenthesis(int character, StsFileBufferedReader reader, List<Map<String, Object>> rawTestSteps) throws IOException {
        // usually after it read name of test step, the next char must be '{'
        if (--bracketsNumber == 0) {
            Map<String, Object> rawTestStep = new HashMap<>();
            rawTestStep.put(paramName, rawTestStepBuilder.toString());

            reader.readUntilFindCharacter('{');
            openedBrackets++;

            paramName = null;
            rawTestSteps.add(readMap(reader, rawTestStep));
        } else {
            addCharacter(character);
        }
    }

    private void addParameterWithValue(Map<String, Object> rawTestStep) {
        rawTestStep.put(paramName, rawTestStepBuilder.toString());
        rawTestStepBuilder.setLength(0);
        paramName = null;
    }

    private void addCharacter(int character) {
        if ((paramName != null && rawTestStepBuilder.length() > 0) ||
                (character != ' ' && character != '\n')) {
            rawTestStepBuilder.append((char) character);
        }
    }

    private boolean isPreviousCharacterNotParameterSign() {
        return rawTestStepBuilder.charAt(rawTestStepBuilder.length() - 1) != '$';
    }

    @Override
    public void close() {
        rawTestStepBuilder.setLength(0);
    }
}