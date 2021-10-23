package org.skellig.teststep.runner.registry

import java.net.URI

internal interface RawTestStepsReader {
    fun getTestStepsFromUri(rootUri: URI): Collection<Map<String, Any?>>
}