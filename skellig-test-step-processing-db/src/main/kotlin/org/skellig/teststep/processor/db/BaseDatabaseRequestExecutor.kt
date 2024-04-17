package org.skellig.teststep.processor.db


/**
 * Base class for executing database requests.
 *
 * This class provides a common implementation of the [DatabaseRequestExecutor] interface.
 * Subclasses of this class can override the [getParameterValue] method to provide custom
 * parameter handling.
 */
abstract class BaseDatabaseRequestExecutor : DatabaseRequestExecutor {

    companion object {
        private const val VALUE = "value"
    }

    protected open fun getParameterValue(item: Any?): Any? {
        if (item is Map<*, *> && item.containsKey(VALUE)) {
            return item[VALUE]
        }
        return item
    }

    override fun close() {}
}