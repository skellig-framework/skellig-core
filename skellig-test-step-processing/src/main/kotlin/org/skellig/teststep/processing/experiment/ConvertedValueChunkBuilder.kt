package org.skellig.teststep.processing.experiment

import java.util.concurrent.atomic.AtomicInteger

class ConvertedValueChunkBuilder {

    fun parseProperty(value: String, index: AtomicInteger, parameters: Map<String, Any?>): ConvertedValueChunk {
        var key = ""
        var default: ConvertedValueChunk? = null
        while (index.get() < value.length) {
            when (val c = value[index.get()]) {
                // default value of property declaration
                ':' -> {
                    index.incrementAndGet()
                    default = parse(value, index, parameters)
                    break
                }
                // end of property declaration
//                '}', ']', ')' -> {
                '}' -> {
                    break
                }
                else -> {
                    key += c
                }
            }
            index.incrementAndGet()
        }
        return PropertyValue(key, default, parameters)
    }

    fun parseFunction(name: String, value: String, index: AtomicInteger, parameters: Map<String, Any?>): FunctionValue {
        val args = mutableListOf<ConvertedValueChunk>()
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
                        args.add(parse(arg, AtomicInteger(0), parameters))
                        arg = ""
                    } else {
                        arg += c
                    }
                }
                else -> {
                    if (c == '(' || c == '{' || c == '[') bracketsCount++
                    else if (c == '}' || c == ']') bracketsCount--
                    arg += c
                }
            }
            index.incrementAndGet()
        }

        if (arg.trim().isNotEmpty()) {
            args.add(parse(arg, AtomicInteger(0), parameters))
        }

        return FunctionValue(name, args.toTypedArray())
    }

    fun parse(value: String, index: AtomicInteger, parameters: Map<String, Any?>): ConvertedValueChunk {
        val trimValue = value.trim()
        var chunk = ""
        var isExtraction = false
        val compositeChunk = CompositeConvertedValue()
        while (index.get() < trimValue.length) {
            val c = trimValue[index.get()]
            if (c == '$' && trimValue[index.get() + 1] == '{') {
                index.set(index.get() + 2)
                if (chunk.trim().isNotEmpty()) {
                    compositeChunk.append(SimpleValue(chunk.trim()))
                    chunk = ""
                }
                compositeChunk.append(parseProperty(trimValue, index, parameters))
            } else if (c == '(' && chunk.trim().isNotEmpty()) {
                index.incrementAndGet()
                if (isExtraction) {
                    compositeChunk.appendExtraction(parseFunction(chunk, trimValue, index, parameters))
                } else {
                    compositeChunk.append(parseFunction(chunk, trimValue, index, parameters))
                }
                chunk = ""
            } else if (c == '#' && trimValue[index.get() + 1] == '[') {
                if (chunk.trim().isNotEmpty()) {
                    compositeChunk.append(SimpleValue(chunk.trim()))
                    chunk = ""
                }
                index.set(index.get() + 2)

                if (isExtraction) {
                    compositeChunk.appendExtraction(parse(trimValue, index, parameters))
                } else {
                    compositeChunk.append(parse(trimValue, index, parameters))
                }
            } else if (c == ']' || c == '}' || c == ')') {
                break
            } else if (c == '.') {
                if (chunk.trim().isNotEmpty()) {
                    if (isExtraction) {
                        compositeChunk.appendExtraction(SimpleValue(chunk.trim()))
                    } else {
                        compositeChunk.append(SimpleValue(chunk.trim()))
                    }
                    chunk = ""
                }
                isExtraction = true
            } else {
                chunk += c
            }
            index.incrementAndGet()
        }
        if (chunk.trim().isNotEmpty()) {
            if (isExtraction) {
                compositeChunk.appendExtraction(SimpleValue(chunk.trim()))
            } else {
                compositeChunk.append(SimpleValue(chunk.trim()))
            }
        }
        return compositeChunk
    }

}