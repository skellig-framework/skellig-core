package org.skellig.feature.hook

import java.lang.reflect.Method

class SkelligHook(
    val tags: Set<String>?,
    val method: Method,
    val instance: Any,
    val order: Int = 0,
    val type: Class<out Annotation>
) {
    fun run() {
        method.invoke(instance)
    }
}