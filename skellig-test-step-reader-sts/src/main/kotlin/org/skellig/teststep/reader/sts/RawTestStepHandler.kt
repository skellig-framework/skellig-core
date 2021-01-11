package org.skellig.teststep.reader.sts

import java.io.Closeable
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

class RawTestStepHandler : Closeable {

    private var openedBrackets = 0
    private var bracketsNumber = 0
    private var propertyName: String? = null
    private var isEnclosedText = false
    private var isParameter = false
    private var rawTestStepBuilder: StringBuilder = StringBuilder()
    private val spacesBuilder = StringBuilder()

    @Throws(IOException::class)
    fun handle(character: Char, reader: StsFileBufferedReader, rawTestSteps: MutableList<Map<String, Any?>>) {
        when (character) {
            '#' -> {
                handleCommentCharacter(reader)
            }
            '(' -> {
                handleOpenParenthesis(character)
            }
            ')' -> {
                handleClosedParenthesis(character, reader, rawTestSteps)
            }
            else -> {
                addCharacter(character)
            }
        }
    }

    @Throws(IOException::class)
    private fun readMap(reader: StsFileBufferedReader, rawTestStep: MutableMap<String, Any?>): Map<String, Any?> {
        emptyBuffer()
        var character: Char
        while (reader.read().also { character = it.toChar() } > 0) {
            if (character == '#') {
                handleCommentCharacter(reader)
            } else if (character == '\'') {
                handleSingleQuoteCharacter(rawTestStep)
            } else if (!isEnclosedText) {
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
        val result: MutableList<Any> = ArrayList()
        while (reader.read().also { character = it.toChar() } > 0) {
            if (character == '#') {
                handleCommentCharacter(reader)
            } else if (character == '{') {
                handleListOpenedCurlyBracketCharacter(character, reader, result)
            } else if (character == '\n' && rawTestStepBuilder.isNotEmpty()) {
                result.add(rawTestStepBuilder.toString())
                emptyBuffer()
            } else if (character == ']') {
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
            result.add(readMap(reader, HashMap()))
            emptyBuffer()
        } else {
            addCharacter(character)
        }
    }

    private fun handleClosedBracketCharacter(character: Char, rawTestStep: MutableMap<String, Any?>): Boolean {
        if (!isParameter) {
            // for '}' if paramName is not null then add its value
            openedBrackets--
            if (propertyName != null) {
                addParameterWithValue(rawTestStep)
            }
            return true
        } else {
            // if '}' is part of parametrised value than close this parameter and include it in the future value
            isParameter = false
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
            isParameter = true
            addCharacter(character)
        } else {
            openedBrackets++
            rawTestStep[rawTestStepBuilder.toString()] = readMap(reader, HashMap())
            emptyBuffer()
        }
    }

    private fun handleSingleQuoteCharacter(rawTestStep: MutableMap<String, Any?>) {
        // Single quote character means that we need to read the value till next single quote
        isEnclosedText = !isEnclosedText
        if (!isEnclosedText && propertyName != null) {
            addParameterWithValue(rawTestStep)
        }
    }

    @Throws(IOException::class)
    private fun handleCommentCharacter(reader: StsFileBufferedReader) {
        reader.readUntilFindCharacter('\n')
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
            val rawTestStep: MutableMap<String, Any?> = HashMap()
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
        rawTestStep[propertyName!!] = rawTestStepBuilder.toString()
        emptyBuffer()
        propertyName = null
    }

    private fun addCharacter(character: Char) {
        if (isValueOfPropertyUnderConstruction() || character != ' ' && character != '\n') {
            if (!isValueOfPropertyUnderConstruction() && spacesBuilder.isNotEmpty()) {
                rawTestStepBuilder.append(spacesBuilder.toString())
                spacesBuilder.setLength(0)
            }
            rawTestStepBuilder.append(character.toChar())
        }
        if (rawTestStepBuilder.isNotEmpty() && character == ' ') {
            spacesBuilder.append(character.toChar())
        }
    }

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