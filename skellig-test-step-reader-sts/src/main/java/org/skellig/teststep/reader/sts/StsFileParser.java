package org.skellig.teststep.reader.sts;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StsFileParser {

    public List<Map<String, Object>> parse(Path filePath) {
        StringBuilder rawTestStepBuilder = new StringBuilder();
        List<Map<String, Object>> rawTestSteps = new ArrayList<>();

        try (BufferedReader bufferedReader = Files.newBufferedReader(filePath)) {
            String paramName = null;
            int character;
            int bracketsNumber = 0;
            while ((character = bufferedReader.read()) > 0) {
                if (character == '#') {
                    readUntilFindCharacter(bufferedReader, '\n');
                    continue;
                } else if (character == '(') {
                    if (bracketsNumber++ == 0) {
                        paramName = rawTestStepBuilder.toString();
                        rawTestStepBuilder.setLength(0);
                        continue;
                    }
                } else if (character == ')') {
                    if (--bracketsNumber == 0) {
                        StsFileParsingDetails stsFileParsingDetails = new StsFileParsingDetails(bufferedReader, rawTestStepBuilder);
                        stsFileParsingDetails.openBrackets();

                        Map<String, Object> rawTestStep = new HashMap<>();
                        rawTestStep.put(paramName, rawTestStepBuilder.toString());

                        readUntilFindCharacter(bufferedReader, '{');

                        rawTestSteps.add(read(stsFileParsingDetails, rawTestStep));
                        paramName = null;
                        continue;
                    }
                }
                if ((paramName != null && rawTestStepBuilder.length() > 0) ||
                        (character != ' ' && character != '\n')) {
                    rawTestStepBuilder.append((char) character);
                }
            }
            return rawTestSteps;
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, Object> read(StsFileParsingDetails fileParsingDetails,
                                     Map<String, Object> rawTestStep) throws IOException {
        StringBuilder rawTestStepBuilder = fileParsingDetails.getRawTestStepBuilder();
        BufferedReader reader = fileParsingDetails.getBufferedReader();
        String paramName = null;
        String closureParamName;
        boolean isEnclosedText = false;
        boolean isParameter = false;
        int character;
        rawTestStepBuilder.setLength(0);

        while ((character = reader.read()) > 0) {
//            if (character != ' ') {
            if (character == '#') {
                readUntilFindCharacter(reader, '\n');
                continue;
            } else if (character == '\'') {
                isEnclosedText = !isEnclosedText;
                if (!isEnclosedText) {
                    if (paramName != null) {
                        rawTestStep.put(paramName, rawTestStepBuilder.toString());
                        rawTestStepBuilder.setLength(0);
                        paramName = null;
                    }
                }
                continue;
            } else if (!isEnclosedText) {
                if (character == '}') {
                    if (!isParameter) {
                        fileParsingDetails.closeBracket();
                        break;
                    } else {
                        isParameter = false;
                    }
                } else if (character == '{') {
                    if (rawTestStepBuilder.length() > 0 &&
                            rawTestStepBuilder.charAt(rawTestStepBuilder.length() - 1) == '$') {
                        isParameter = true;
                    } else {
                        fileParsingDetails.openBrackets();
                        closureParamName = rawTestStepBuilder.toString();
                        rawTestStep.put(closureParamName, read(fileParsingDetails, new HashMap<>()));
                        rawTestStepBuilder.setLength(0);
                        continue;
                    }
                } else if (character == '=') {
                    paramName = rawTestStepBuilder.toString();
                    rawTestStepBuilder.setLength(0);
                    skipAllEmptyCharacters(reader);
                    continue;
                } else if (character == '[') {
                    paramName = rawTestStepBuilder.toString();
                    rawTestStep.put(paramName, readList(fileParsingDetails));
                    rawTestStepBuilder.setLength(0);
                    paramName = null;
                    continue;
                } else if (character == '\n') {
                    if (paramName != null) {
                        rawTestStep.put(paramName, rawTestStepBuilder.toString());
                        rawTestStepBuilder.setLength(0);
                        paramName = null;
                        continue;
                    }
                }
            }

            if ((paramName != null && rawTestStepBuilder.length() > 0) ||
                    (character != ' ' && character != '\n')) {
                rawTestStepBuilder.append((char) character);
            }

//            }
        }
        return rawTestStep;
    }

    private List<String> readList(StsFileParsingDetails fileParsingDetails) throws IOException {
        StringBuilder rawTestStepBuilder = fileParsingDetails.getRawTestStepBuilder();
        BufferedReader reader = fileParsingDetails.getBufferedReader();
        int character;
        rawTestStepBuilder.setLength(0);
        List<String> result = new ArrayList<>();
        while ((character = reader.read()) > 0) {
            if (character == '#') {
                readUntilFindCharacter(reader, '\n');
                continue;
            } else if (character == '\n' && rawTestStepBuilder.length() > 0) {
                result.add(rawTestStepBuilder.toString());
                rawTestStepBuilder.setLength(0);
                continue;
            } else if (character == ']') {
                break;
            }

            if (rawTestStepBuilder.length() > 0 || character != ' ' && character != '\n') {
                rawTestStepBuilder.append((char) character);
            }

        }
        return result;
    }

    private void readUntilFindCharacter(BufferedReader bufferedReader, char c) throws IOException {
        while (bufferedReader.read() != c) ;
    }

    private void skipAllEmptyCharacters(BufferedReader bufferedReader) throws IOException {
        int c;
        while ((c = bufferedReader.read()) > 0) {
            if (c != ' ' || c != '\n') {
                break;
            }
        }
    }

    private static class StsFileParsingDetails {

        private BufferedReader bufferedReader;
        private StringBuilder rawTestStepBuilder;
        private int openedBrackets;

        public StsFileParsingDetails(BufferedReader bufferedReader, StringBuilder rawTestStepBuilder) {
            this.bufferedReader = bufferedReader;
            this.rawTestStepBuilder = rawTestStepBuilder;
        }

        public BufferedReader getBufferedReader() {
            return bufferedReader;
        }

        public StringBuilder getRawTestStepBuilder() {
            return rawTestStepBuilder;
        }

        public int getOpenedBrackets() {
            return openedBrackets;
        }

        public void openBrackets() {
            openedBrackets++;
        }

        public void closeBracket() {
            openedBrackets--;
        }

        boolean allBracketsClosed() {
            return openedBrackets == 0;
        }
    }

}
