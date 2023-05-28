package org.skellig.teststep.processing.value.extractor

class DefaultValueExtractor
private constructor(private val extractors: Map<String, ValueExtractor>) : ValueExtractor {

    override fun extractFrom(name: String, value: Any?, args: Array<Any?>): Any? {
        return if (extractors.containsKey(name)) extractors[name]?.extractFrom(name, value, args)
        else extractors[""]?.extractFrom(name, value, args)
    }

    override fun getExtractFunctionName(): String {
        return ""
    }

    class Builder {
        private val extractors = mutableMapOf<String, ValueExtractor>()

        fun withValueExtractor(valueExtractor: ValueExtractor) = apply {
            extractors[valueExtractor.getExtractFunctionName()] = valueExtractor
        }

        fun build(): ValueExtractor {
            val objectValueExtractor = ObjectValueExtractor()
            withValueExtractor(JsonPathValueExtractor())
            withValueExtractor(JsonToMapTestStepValueExtractor())
            withValueExtractor(JsonToListTestStepValueExtractor())
            withValueExtractor(XPathValueExtractor())
            withValueExtractor(objectValueExtractor)
            withValueExtractor(FindFromStateValueExtractor(objectValueExtractor))
            withValueExtractor(FromIndexValueExtractor())
            withValueExtractor(ToStringValueExtractor())
            withValueExtractor(SubStringValueExtractor())
            withValueExtractor(SubStringLastValueExtractor())
            withValueExtractor(PlusOperatorTestStepValueExtractor())
            withValueExtractor(MinusOperatorTestStepValueExtractor())
            withValueExtractor(TimesOperatorTestStepValueExtractor())
            withValueExtractor(DivOperatorTestStepValueExtractor())
            withValueExtractor(PowOperatorTestStepValueExtractor())
            withValueExtractor(ToIntTestStepValueExtractor())
            withValueExtractor(ToLongTestStepValueExtractor())
            withValueExtractor(FromRegexValueExtractor())
            withValueExtractor(ToByteTestStepValueExtractor())
            withValueExtractor(ToShortTestStepValueExtractor())
            withValueExtractor(ToFloatTestStepValueExtractor())
            withValueExtractor(ToDoubleTestStepValueExtractor())
            withValueExtractor(ToBooleanTestStepValueExtractor())
            withValueExtractor(ToBigDecimalTestStepValueExtractor())
            withValueExtractor(ToDateTimeValueExtractor())
            withValueExtractor(ToDateValueExtractor())
            withValueExtractor(ToBytesValueExtractor())
            withValueExtractor(FormatDateValueExtractor())
            withValueExtractor(FormatDateTimeValueExtractor())

            return DefaultValueExtractor(extractors)
        }
    }
}