package org.skellig.teststep.processing.value.extractor

import org.skellig.teststep.processing.value.extractor.collection.*

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
            val objectValueExtractor = NewObjectValueExtractor()
            withValueExtractor(JsonPathValueExtractor())
            withValueExtractor(JsonToMapTestStepValueExtractor())
            withValueExtractor(JsonToListTestStepValueExtractor())
            withValueExtractor(XPathValueExtractor())
            withValueExtractor(objectValueExtractor)
            withValueExtractor(ValuesValueExtractor())
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
            withValueExtractor(SizeValueExtractor())
            withValueExtractor(AllFunctionExecutor())
            withValueExtractor(AnyFunctionExecutor())
            withValueExtractor(NoneFunctionExecutor())
            withValueExtractor(CountFunctionExecutor())
            withValueExtractor(FindAllFunctionExecutor())
            withValueExtractor(FindFunctionExecutor())
            withValueExtractor(FindLastFunctionExecutor())
            withValueExtractor(GroupByFunctionExecutor())
            withValueExtractor(MapFunctionExecutor())
            withValueExtractor(MinOfFunctionExecutor())
            withValueExtractor(MaxOfFunctionExecutor())
            withValueExtractor(SortFunctionExecutor())
            withValueExtractor(SumOfFunctionExecutor())

            return DefaultValueExtractor(extractors)
        }
    }
}