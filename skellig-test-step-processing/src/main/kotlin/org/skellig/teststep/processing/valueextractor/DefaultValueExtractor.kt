package org.skellig.teststep.processing.valueextractor

import org.skellig.teststep.processing.experiment.ValueExtractor

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
            withValueExtractor(ConcatTestStepValueExtractor())
            withValueExtractor(JsonPathTestStepValueExtractor())
            withValueExtractor(JsonToMapTestStepValueExtractor())
            withValueExtractor(JsonToListTestStepValueExtractor())
            withValueExtractor(XPathTestStepValueExtractor())
            withValueExtractor(ObjectTestStepValueExtractor())
            withValueExtractor(FromIndexTestStepValueExtractor())
            withValueExtractor(ToStringTestStepValueExtractor())
            withValueExtractor(SubStringTestStepValueExtractor())
            withValueExtractor(SubStringLastTestStepValueExtractor())
            withValueExtractor(PlusOperatorTestStepValueExtractor())
            withValueExtractor(MinusOperatorTestStepValueExtractor())
            withValueExtractor(TimesOperatorTestStepValueExtractor())
            withValueExtractor(DivOperatorTestStepValueExtractor())
            withValueExtractor(PowOperatorTestStepValueExtractor())
            withValueExtractor(ToIntTestStepValueExtractor())
            withValueExtractor(ToLongTestStepValueExtractor())
            withValueExtractor(RegexTestStepValueExtractor())
            withValueExtractor(ToByteTestStepValueExtractor())
            withValueExtractor(ToShortTestStepValueExtractor())
            withValueExtractor(ToFloatTestStepValueExtractor())
            withValueExtractor(ToDoubleTestStepValueExtractor())
            withValueExtractor(ToBooleanTestStepValueExtractor())
            withValueExtractor(ToBigDecimalTestStepValueExtractor())
            withValueExtractor(ToDateTimeValueExtractor())
            withValueExtractor(ToDateValueExtractor())
            withValueExtractor(ToBytesValueExtractor())

            return DefaultValueExtractor(extractors)
        }
    }
}