package org.skellig.teststep.processor.db.model

/**
 * This class represents a Database Request.
 *
 * A Database Request can be used to create database queries or commands.
 *
 * The properties [command], [table] and [columnValuePairs] must be defined together or [query] with [queryParameters].
 *
 * @constructor Creates a DatabaseRequest object with the specified SQL query and query parameters.
 * @param query SQL query string.
 * @param queryParameters The list of query parameters.
 *
 * @constructor Creates a DatabaseRequest object with the specified command, table, and column-value pairs.
 * @param command The command string.
 * @param table The table name.
 * @param columnValuePairs The map of column-value pairs.
 */
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