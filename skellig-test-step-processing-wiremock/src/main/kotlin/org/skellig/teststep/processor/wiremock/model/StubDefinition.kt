package org.skellig.teststep.processor.wiremock.model

/** A request match paired with a canned response, as declared in the `stub {}` block of a step. */
internal data class StubDefinition(val request: StubRequest, val response: StubResponse)

internal data class StubRequest(val method: String, val path: String)

internal data class StubResponse(val status: Int, val headers: Map<String, String>, val body: String?)
