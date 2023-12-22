package org.skellig.teststep.processing.value.converter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule

class TestDataToJsonConverter : TestDataConverter {

    companion object {
        private const val NAME = "toJson"
    }

    private val jsonSerializer = ObjectMapper()

    init {
        jsonSerializer.registerModule(JavaTimeModule())
        jsonSerializer.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun convert(data: Any?): Any? {
        return (data as? Map<*, *>)?.let {
            if (it.containsKey(NAME)) jsonSerializer.writeValueAsString(data[NAME])
            else jsonSerializer.writeValueAsString(data)
        }
    }

    override fun getName(): String = NAME

    override fun isApplicable(data: Any?): Boolean {
        return data is Map<*, *>&& data.size == 1 && data.containsKey(NAME)
    }

}