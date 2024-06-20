package org.skellig.teststep.processor.http.model

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class HttpTestStepTest {

    companion object {
        private const val SRV_1 = "srv1"
        private const val SRV_2 = "srv2"
    }

    @Test
    fun `verify toString of Test Step`() {
        assertEquals(
            "name = n1\n" +
                    "services = [srv1, srv2]\n" +
                    "url = /a/b/c\n" +
                    "method = post\n",
            HttpTestStep.Builder()
                .withService(listOf(SRV_1, SRV_2))
                .withUrl("/a/b/c")
                .withMethod("post")
                .withName("n1")
                .build()
                .toString()
        )

        assertEquals(
            "name = n2\n" +
                    "url = /a?x=100\n" +
                    "method = GET\n" +
                    "username = user1\n" +
                    "password = user1\n" +
                    "headers {\n" +
                    "  Content-Type=html/text\n" +
                    "  x-type=ABC\n" +
                    "}\n" +
                    "query {\n" +
                    "  n=400\n" +
                    "}\n" +
                    "form {\n" +
                    "  f=100\n" +
                    "}\n",
            HttpTestStep.Builder()
                .withUrl("/a?x=100")
                .withMethod("GET")
                .withUsername("user1")
                .withPassword("user1")
                .withHeaders(mapOf(Pair("Content-Type", "html/text"), Pair("x-type", "ABC")))
                .withQuery(mapOf(Pair("n", "400")))
                .withForm(mapOf(Pair("f", "100")))
                .withName("n2")
                .build()
                .toString()
        )
    }
}