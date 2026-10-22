package org.skellig.teststep.processor.wiremock.model

/** What a `wiremock = reset` step clears on a mock server. */
enum class ResetScope(val keyword: String) {
    ALL("all"), STUBS("stubs"), JOURNAL("journal");

    companion object {
        fun fromKeyword(keyword: String) = entries.firstOrNull { it.keyword == keyword.trim().lowercase() }
    }
}
