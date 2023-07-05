package org.skellig.runner.junit.report.attachment

interface AttachmentService<T : ReportAttachment<*>> {
    fun getData(): T
}

object AttachmentServices {
    val attachmentServices: List<AttachmentService< ReportAttachment<*>>> = TODO()
}
