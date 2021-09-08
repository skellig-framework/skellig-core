package org.skellig.teststep.processor.db.model

open class DatabaseRequest private constructor(val query: String? = null,
                                               val queryParameters: List<Any?>? = null,
                                               val command: String? = null,
                                               val table: String? = null,
                                               val columnValuePairs: Map<String, Any?>? = null) {

    constructor(query: String?, queryParameters: List<Any?>? = null) : this(query, queryParameters, null, null, null)

    constructor(command: String? = null,
                table: String? = null,
                columnValuePairs: Map<String, Any?>? = null) : this(null, null, command, table, columnValuePairs)

}