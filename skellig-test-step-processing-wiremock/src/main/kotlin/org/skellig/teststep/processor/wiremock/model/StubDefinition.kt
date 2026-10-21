package org.skellig.teststep.processor.wiremock.model

/** A request match paired with a canned response, as declared in the `stub {}` block of a step. */
internal data class StubDefinition(val request: StubRequest, val response: StubResponse, val priority: Int? = null)

internal data class StubRequest(
    val method: String,
    val path: RequestMatcher,
    val query: Map<String, RequestMatcher> = emptyMap(),
    val headers: Map<String, RequestMatcher> = emptyMap(),
    val body: RequestMatcher? = null
)

internal sealed interface RequestMatcher {
    data class Equal(val value: String) : RequestMatcher
    data class Contains(val value: String) : RequestMatcher
    data class Matches(val value: String) : RequestMatcher
    data class Json(val value: String) : RequestMatcher
}

internal data class StubResponse(val status: Int, val headers: Map<String, String>, val body: String?, val delay: Int? = null)
