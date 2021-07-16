package org.skellig.teststep.reader.sts

import java.io.*

class StsFileBufferedReader(inputStream: InputStream) : BufferedInputStream(inputStream) {

    @Throws(IOException::class)
    fun readUntilFindCharacter(character: Char) {
        var c: Int
        while (read().also { c = it } > 0) {
            if (c == character.code) {
                break
            }
        }
    }
}