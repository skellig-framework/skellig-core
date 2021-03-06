package org.skellig.teststep.reader.sts

import java.io.Closeable
import java.io.IOException
import kotlin.collections.set

class RawTestStepHandler : Closeable {

    companion object {
        private const val NULL = "null"
    }

    private var isSpecialCharacter: Boolean = false     // ",',\,\n,\r,\t,
    private var openedBrackets = 0
    private var bracketsNumber = 0
    private var parameterBrackets = 0
    private var propertyName: String? = null
    private var isEnclosedText = false
    private var rawTestStepBuilder: StringBuilder = StringBuilder()
    private val spacesBuilder = StringBuilder()

    @Throws(IOException::class)
    fun handle(character: Char, reader: StsFileBufferedReader, rawTestSteps: MutableList<Map<String, Any?>>) {
        when (character) {
            '#' -> handleCommentCharacter(reader)
            '(' -> handleOpenParenthesis(character)
            ')' -> handleClosedParenthesis(character, reader, rawTestSteps)
            else -> addCharacter(character)
        }
    }

    @Throws(IOException::class)
    private fun readMap(reader: StsFileBufferedReader, rawTestStep: MutableMap<String, Any?>): Map<String, Any?> {
        emptyBuffer()
        var character: Char
        while (reader.read().also { character = it.toChar() } > 0) {
            if (character == '#' && !isEnclosedText) {
                handleCommentCharacter(reader, rawTestStep)
            } else if (!isSpecialCharacter && isEnclosedStringCharacter(character)) {
                handleSingleQuoteCharacter()
            } else if (!isEnclosedText) {  // skip handling special characters if enclosed in single quotes
                if (character == '}') {
                    if (handleClosedBracketCharacter(character, rawTestStep)) break
                } else if (character == '{') {
                    handleOpenCurlyBracketCharacter(character, reader, rawTestStep)
                } else if (character == '=') {
                    handleEqualSignCharacter()
                } else if (character == '[' && rawTestStepBuilder.isNotEmpty()) {
                    handleArrayBracketCharacter(reader, rawTestStep)
                } else if (character == '\n') {
                    handleNewLineCharacter(character, rawTestStep)
                } else {
                    addCharacter(character)
                }
            } else {
                addCharacter(character)
            }
        }
        return rawTestStep
    }

    @Throws(IOException::class)
    private fun readList(reader: StsFileBufferedReader): List<Any> {
        emptyBuffer()
        var character: Char
        val result = mutableListOf<Any>()
        while (reader.read().also { character = it.toChar() } > 0) {
            if (character == '#' && !isEnclosedText) {
                handleCommentCharacter(reader)
            } else if (!isSpecialCharacter && isEnclosedStringCharacter(character)) {
                handleSingleQuoteCharacter()
            } else if (character == '{') {
                handleListOpenedCurlyBracketCharacter(character, reader, result)
            } else if (character == '\n' && rawTestStepBuilder.isNotEmpty()) {
                result.add(rawTestStepBuilder.toString())
                emptyBuffer()
            } else if (character == ']') {
                if (rawTestStepBuilder.isNotEmpty()) {
                    result.add(rawTestStepBuilder.toString())
                }
                break
            } else {
                addCharacter(character)
            }
        }
        return result
    }

    @Throws(IOException::class)
    private fun handleListOpenedCurlyBracketCharacter(character: Char, reader: StsFileBufferedReader, result: MutableList<Any>) {
        // '{' for list-type value means that its item will be Map
        if (rawTestStepBuilder.isEmpty() || isPreviousCharacterNotParameterSign()) {
            openedBrackets++
            result.add(readMap(reader, hashMapOf()))
            emptyBuffer()
        } else {
            addCharacter(character)
        }
    }

    private fun handleClosedBracketCharacter(character: Char, rawTestStep: MutableMap<String, Any?>): Boolean {
        if (parameterBrackets<=0) {
            // for '}' if paramName is not null then add its value
            openedBrackets--
            parameterBrackets = 0
            if (propertyName != null) {
                addParameterWithValue(rawTestStep)
            }
            return true
        } else {
            // if '}' is part of parametrised value than close this parameter and include it in the future value
            parameterBrackets--
            addCharacter(character)
        }
        return false
    }

    private fun handleNewLineCharacter(character: Char, rawTestStep: MutableMap<String, Any?>) {
        // skip it if there is nothing to add to the builder
        if (rawTestStepBuilder.isNotEmpty()) {
            // if paramName is read before '=' then we can add value to it.
            // Otherwise just add the character - usually when text is enclosed in single quotes
            if (propertyName != null) {
                addParameterWithValue(rawTestStep)
            } else {
                addCharacter(character)
            }
        }
    }

    @Throws(IOException::class)
    private fun handleArrayBracketCharacter(reader: StsFileBufferedReader, rawTestStep: MutableMap<String, Any?>) {
        rawTestStep[rawTestStepBuilder.toString()] = readList(reader)
        emptyBuffer()
    }

    private fun handleEqualSignCharacter() {
        // after '=' sign we can set paramName and continue reading its value
        propertyName = rawTestStepBuilder.toString()
        emptyBuffer()
    }

    @Throws(IOException::class)
    private fun handleOpenCurlyBracketCharacter(character: Char, reader: StsFileBufferedReader, rawTestStep: MutableMap<String, Any?>) {
        // if it's a parameter then continue reading.
        // Otherwise start read value as Map
        if (rawTestStepBuilder.isNotEmpty() && !isPreviousCharacterNotParameterSign()) {
            parameterBrackets++
            addCharacter(character)
        } else {
            openedBrackets++
            rawTestStep[rawTestStepBuilder.toString()] = readMap(reader, hashMapOf())
            emptyBuffer()
        }
    }

    private fun handleSingleQuoteCharacter() {
        // Single quote character means that we need to read the value till the next single quote
        isEnclosedText = !isEnclosedText
    }

    private fun handleCommentCharacter(reader: StsFileBufferedReader) {
        reader.readUntilFindCharacter('\n')
    }

    @Throws(IOException::class)
    private fun handleCommentCharacter(reader: StsFileBufferedReader, rawTestStep: MutableMap<String, Any?>) {
        reader.readUntilFindCharacter('\n')
        //TODO: if comment goes after value enclosed in quotes, it can trim spaces
        // in case if comment goes after the value, try to trim spaces
        val value = rawTestStepBuilder.trim()
        if (value.isNotEmpty()) {
            // if value not empty then assign it
            rawTestStepBuilder = rawTestStepBuilder.clear().append(value)
            handleNewLineCharacter('\n', rawTestStep)
        }
    }

    private fun handleOpenParenthesis(character: Char) {
        // can be name of test step, or anything else, ex: regex, function, etc.
        if (bracketsNumber++ == 0) {
            propertyName = rawTestStepBuilder.toString()
            emptyBuffer()
        } else {
            addCharacter(character)
        }
    }

    @Throws(IOException::class)
    private fun handleClosedParenthesis(character: Char, reader: StsFileBufferedReader, rawTestSteps: MutableList<Map<String, Any?>>) {
        // usually after it read name of test step, the next char must be '{'
        if (--bracketsNumber == 0) {
            val rawTestStep = hashMapOf<String, Any?>()
            rawTestStep[propertyName!!] = rawTestStepBuilder.toString()
            reader.readUntilFindCharacter('{')
            openedBrackets++
            propertyName = null
            rawTestSteps.add(readMap(reader, rawTestStep))
        } else {
            addCharacter(character)
        }
    }

    private fun addParameterWithValue(rawTestStep: MutableMap<String, Any?>) {
        val value = rawTestStepBuilder.toString()
        rawTestStep[propertyName!!] = when {
            value == NULL -> null
            // if left-over spaces were not registered then the value was enclosed in single quotes
            // thus no need preserve them and not to trim
            spacesBuilder.isEmpty() -> value
            else -> value.trim()
        }
        emptyBuffer()
        propertyName = null
    }

    private fun addCharacter(character: Char) {
        if (!isSpecialCharacter && isEnclosedStringCharacter(character)) {
            return
        }

        if (!isSpecialCharacter && isEnclosedText && character == '\\') {
            isSpecialCharacter = true
        } else if (isSpecialCharacter) {
            isSpecialCharacter = false
            when (character) {
                'n' -> rawTestStepBuilder.append("\n")
                't' -> rawTestStepBuilder.append("\t")
                'r' -> rawTestStepBuilder.append("\r")
                '"' -> rawTestStepBuilder.append("\"")
                '\'' -> rawTestStepBuilder.append("'")
                '\\' -> rawTestStepBuilder.append("\\")
                else -> rawTestStepBuilder.append("\\").append(character)
            }
        } else {
            if (isEnclosedText) {
                // if text is inside quotes then just add the character
                rawTestStepBuilder.append(character)
            } else {
                if (isValueOfPropertyUnderConstruction() || character != ' ' && character != '\n') {
                    // add leftover spaces before the character
                    if (!isValueOfPropertyUnderConstruction() && spacesBuilder.isNotEmpty()) {
                        rawTestStepBuilder.append(spacesBuilder.toString())
                        spacesBuilder.setLength(0)
                    }
                    rawTestStepBuilder.append(character)
                }
                if (rawTestStepBuilder.isNotEmpty() && character == ' ') {
                    spacesBuilder.append(character)
                }
            }
        }
    }

    private fun isEnclosedStringCharacter(character: Char) = character == '\'' || character == '\"'

    private fun isValueOfPropertyUnderConstruction(): Boolean {
        return propertyName != null && rawTestStepBuilder.isNotEmpty()
    }

    private fun isPreviousCharacterNotParameterSign(): Boolean {
        return rawTestStepBuilder[rawTestStepBuilder.length - 1] != '$'
    }

    private fun emptyBuffer() {
        rawTestStepBuilder.setLength(0)
        spacesBuilder.setLength(0)
    }

    override fun close() {
        rawTestStepBuilder.setLength(0)
    }
}