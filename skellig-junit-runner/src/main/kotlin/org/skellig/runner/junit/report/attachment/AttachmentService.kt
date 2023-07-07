package org.skellig.runner.junit.report.attachment

interface AttachmentService<T : ReportAttachment<*>> {
    fun getData(): T
    fun isApplicable(isTestPass: Boolean): Boolean = true
}