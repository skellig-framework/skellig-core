package org.skellig.teststep.reader.sts

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

class StsFileBufferedReader(reader: Reader) : BufferedReader(reader) {

    @Throws(IOException::class)
    fun readUntilFindCharacter(character: Char) {
        var c: Int
        while (read().also { c = it } > 0) {
            if (c == character.toInt()) {
                break
            }
        }
    }
}