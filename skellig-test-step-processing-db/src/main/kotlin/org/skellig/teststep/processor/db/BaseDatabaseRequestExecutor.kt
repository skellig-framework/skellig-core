package org.skellig.teststep.processor.db

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