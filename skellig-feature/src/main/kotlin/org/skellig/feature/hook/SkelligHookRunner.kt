package org.skellig.feature.hook

interface SkelligHookRunner {

    fun run(tags: Set<String>?, hookType: Class<out Annotation>, onRunCompleted: (String, Throwable?, Long) -> Unit)

}
