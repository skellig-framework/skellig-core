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
                    handleEqualSignCharacter(reader);
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
        if (rawTestStepBuilder.length() == 0 || rawTestStepBuilder.charAt(rawTestStepBuilder.length() - 1) != '$') {
            openedBrackets++;
            result.add(readMap(reader, new HashMap<>()));
            rawTestStepBuilder.setLength(0);
        } else {
            addCharacter(character);
        }
    }

    private boolean handleClosedBracketCharacter(int character, Map<String, Object> rawTestStep) {
        if (!isParameter) {
            openedBrackets--;
            if (paramName != null) {
                rawTestStep.put(paramName, rawTestStepBuilder.toString());
                rawTestStepBuilder.setLength(0);
            }
            return true;
        } else {
            isParameter = false;
            addCharacter(character);
        }
        return false;
    }

    private void handleNewLineCharacter(int character, Map<String, Object> rawTestStep) {
        if (paramName != null) {
            rawTestStep.put(paramName, rawTestStepBuilder.toString());
            rawTestStepBuilder.setLength(0);
            paramName = null;
        } else {
            addCharacter(character);
        }
    }

    private void handleArrayBracketCharacter(StsFileBufferedReader reader, Map<String, Object> rawTestStep) throws IOException {
        paramName = rawTestStepBuilder.toString();
        rawTestStep.put(paramName, readList(reader));
        rawTestStepBuilder.setLength(0);
        paramName = null;
    }

    private void handleEqualSignCharacter(StsFileBufferedReader reader) throws IOException {
        paramName = rawTestStepBuilder.toString();
        rawTestStepBuilder.setLength(0);
        reader.skipAllEmptyCharacters();
    }

    private void handleOpenCurlyBracketCharacter(int character, StsFileBufferedReader reader, Map<String, Object> rawTestStep) throws IOException {
        if (rawTestStepBuilder.length() > 0 &&
                rawTestStepBuilder.charAt(rawTestStepBuilder.length() - 1) == '$') {
            isParameter = true;
            addCharacter(character);
        } else {
            openedBrackets++;
            rawTestStep.put(rawTestStepBuilder.toString(), readMap(reader, new HashMap<>()));
            rawTestStepBuilder.setLength(0);
        }
    }

    private void handleSingleQuoteCharacter(Map<String, Object> rawTestStep) {
        isEnclosedText = !isEnclosedText;
        if (!isEnclosedText) {
            if (paramName != null) {
                rawTestStep.put(paramName, rawTestStepBuilder.toString());
                rawTestStepBuilder.setLength(0);
                paramName = null;
            }
        }
    }

    private void handleCommentCharacter(StsFileBufferedReader reader) throws IOException {
        reader.readUntilFindCharacter('\n');
    }

    private void handleOpenParenthesis(int character) {
        if (bracketsNumber++ == 0) {
            paramName = rawTestStepBuilder.toString();
            rawTestStepBuilder.setLength(0);
        } else {
            addCharacter(character);
        }
    }

    private void handleClosedParenthesis(int character, StsFileBufferedReader reader, List<Map<String, Object>> rawTestSteps) throws IOException {
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

    private void addCharacter(int character) {
        if ((paramName != null && rawTestStepBuilder.length() > 0) ||
                (character != ' ' && character != '\n')) {
            rawTestStepBuilder.append((char) character);
        }
    }

    @Override
    public void close() {
        rawTestStepBuilder.setLength(0);
    }
}