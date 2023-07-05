package org.skellig.runner.junit.report.attachment.log

import org.skellig.runner.junit.report.attachment.ReportAttachment

class LogAttachment(data: List<String>) : ReportAttachment<List<String>>("Logs", data)