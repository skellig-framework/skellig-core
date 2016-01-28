package org.skellig.teststep.reader.sts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

class StsFileBufferedReader extends BufferedReader {

    StsFileBufferedReader(Reader in) {
        super(in);
    }

    void readUntilFindCharacter(char character) throws IOException {
        int c;
        while ((c = read()) > 0) {
            if (c == character) {
                break;
            }
        }
    }

}
