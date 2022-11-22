package org.skellig.teststep.processing.value.chunk

import java.util.concurrent.atomic.AtomicInteger

class RawValueChunkParser {

    companion object {
        val SPECIAL_OPENING_BRACKETS = setOf('(', '{', '[')
        val SPECIAL_CLOSING_BRACKETS = setOf(']', '}')
    }

    fun buildFrom(value: String, parameters: Map<String, Any?>): RawValueChunk = buildFrom(value, AtomicInteger(0), parameters)

    private fun buildFrom(value: String, index: AtomicInteger, parameters: Map<String, Any?>): RawValueChunk {
        var chunk = ""
        var isExtraction = false
        var isInQuotes = false
        val compositeChunk = CompositeRawValue()
        while (index.get() < value.length) {
            val c = value[index.get()]
            if (c == '\'' && (isInQuotes || index.get() == 0 || value[index.get() - 1] != '\\')) {
                isInQuotes = !isInQuotes
            } else if (!isInQuotes) {
                if (c == '$' && value[index.get() + 1] == '{') {
                    index.set(index.get() + 2)
                    chunk = appendNotEmptyChunkTo(chunk, compositeChunk)
                    compositeChunk.append(parseProperty(value, index, parameters))
                } else if (c == '(' && chunk.trim().isNotEmpty()) {
                    index.incrementAndGet()
                    if (isExtraction) {
                        compositeChunk.appendExtraction(parseFunction(chunk, value, index, parameters))
                    } else {
                        compositeChunk.append(parseFunction(chunk, value, index, parameters))
                    }
                    chunk = ""
                } else if (c == '#' && value[index.get() + 1] == '[') {
                    chunk = appendNotEmptyChunkTo(chunk, compositeChunk)
                    index.set(index.get() + 2)

                    if (isExtraction) {
                        compositeChunk.appendExtraction(buildFrom(value, index, parameters))
                    } else {
                        compositeChunk.append(buildFrom(value, index, parameters))
                    }
                } else if (SPECIAL_CLOSING_BRACKETS.contains(c) || c == ')') {
                    break
                } else if (c == '.') {
                    chunk = appendNotEmptyChunkTo(chunk, isExtraction, compositeChunk)
                    isExtraction = true
                } else {
                    chunk += if (c == '\\' && index.get() + 1 < value.length) {
                        value[index.incrementAndGet()]
                    } else c
                }
            } else {
                chunk += if (c == '\\' && index.get() + 1 < value.length) {
                    value[index.incrementAndGet()]
                } else c
            }
            index.incrementAndGet()
        }
        appendNotEmptyChunkTo(chunk, isExtraction, compositeChunk)
        return compositeChunk
    }

    private fun parseProperty(value: String, index: AtomicInteger, parameters: Map<String, Any?>): RawValueChunk {
        var key = ""
        var default: RawValueChunk? = null
        while (index.get() < value.length) {
            when (val c = value[index.get()]) {
                // default value of property declaration
                ':' -> {
                    index.incrementAndGet()
                    default = buildFrom(value, index, parameters)
                    break
                }
                // end of property declaration
                '}' -> {
                    break
                }
                else -> {
                    key += c
                }
            }
            index.incrementAndGet()
        }
        return PropertyValue(key.trim(), default, parameters)
    }

    private fun parseFunction(name: String, value: String, index: AtomicInteger, parameters: Map<String, Any?>): FunctionValue {
        val args = mutableListOf<RawValueChunk>()
        var arg = ""
        var bracketsCount = 1
        while (index.get() < value.length) {
            when (val c = value[index.get()]) {
                // end of function declaration
                ')' -> {
                    bracketsCount--
                    if (bracketsCount == 0) {
                        break
                    } else {
                        arg += c
                    }
                }
                ',' -> {
                    if (bracketsCount == 1) {
                        args.add(buildFrom(arg, AtomicInteger(0), parameters))
                        arg = ""
                    } else {
                        arg += c
                    }
                }
                else -> {
                    if (SPECIAL_OPENING_BRACKETS.contains(c)) bracketsCount++
                    else if (SPECIAL_CLOSING_BRACKETS.contains(c)) bracketsCount--
                    arg += c
                }
            }
            index.incrementAndGet()
        }

        if (arg.trim().isNotEmpty()) {
            args.add(buildFrom(arg, AtomicInteger(0), parameters))
        }

        return FunctionValue(name, args.toTypedArray())
    }

    private fun appendNotEmptyChunkTo(
        chunk: String,
        isExtraction: Boolean,
        compositeChunk: CompositeRawValue
    ): String {
        if (chunk.trim().isNotEmpty()) {
            if (isExtraction) {
                compositeChunk.appendExtraction(SimpleValue(chunk))
            } else {
                compositeChunk.append(SimpleValue(chunk))
            }
        }
        return ""
    }

    private fun appendNotEmptyChunkTo(chunk: String, compositeChunk: CompositeRawValue): String =
        appendNotEmptyChunkTo(chunk, false, compositeChunk)

}