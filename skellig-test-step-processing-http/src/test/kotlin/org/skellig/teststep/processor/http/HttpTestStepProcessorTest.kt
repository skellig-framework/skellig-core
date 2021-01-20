package org.skellig.teststep.processor.http

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.*
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processor.http.model.HttpResponse
import org.skellig.teststep.processor.http.model.HttpTestStep
import java.util.concurrent.atomic.AtomicBoolean

class HttpTestStepProcessorTest {

    companion object {
        private const val SRV_2 = "srv2"
        private const val SRV_1 = "srv1"
    }

    private var processor: TestStepProcessor<HttpTestStep>? = null
    private var httpChannel = mock<HttpChannel>()
    private var httpChannel2 = mock<HttpChannel>()

    @Nested
    internal inner class SingleServiceTest {

        @BeforeEach
        fun setUp() {
            val channels = mapOf( Pair("srv1", httpChannel))
            processor = HttpTestStepProcessor(channels, mock(), mock(), mock())
        }

        @Test
        @DisplayName("Send HTTP request to a single service Then verify response received un-grouped")
        fun testSendHttpRequest() {
            val httpTestStep = HttpTestStep.Builder()
                    .withService(listOf(SRV_1))
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()

            val response1 = HttpResponse.Builder().build()
            whenever(httpChannel.send(argThat { r -> r.url == "/a/b/c" })).thenReturn(response1)

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertEquals(response1, r)
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
        }

        @Test
        @DisplayName("Send HTTP request When no service provided Then verify it was sent to a single default registered service")
        fun testSendHttpRequestWhenNoServiceProvided() {
            val httpTestStep = HttpTestStep.Builder()
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()

            whenever(httpChannel.send(anyOrNull())).thenReturn(HttpResponse.Builder().build())

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertNotNull(r)
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
        }
    }

    @Nested
    internal inner class MultipleServiceTest {

        @BeforeEach
        fun setUp() {
            val channels = mapOf(
                    Pair("srv1", httpChannel),
                    Pair("srv2", httpChannel2))
            processor = HttpTestStepProcessor(channels, mock(), mock(), mock())
        }

        @Test
        @DisplayName("Send HTTP request to 2 service Then verify response received from both")
        fun testSendHttpRequestToAllServices() {
            val httpTestStep = HttpTestStep.Builder()
                    .withService(listOf(SRV_1, SRV_2))
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()
            val response1 = HttpResponse.Builder().build()
            val response2 = HttpResponse.Builder().build()

            whenever(httpChannel.send(anyOrNull())).thenReturn(response1)
            whenever(httpChannel2.send(anyOrNull())).thenReturn(response2)

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, r, _ ->
                        Assertions.assertEquals(response1, (r as Map<*, *>?)!![SRV_1])
                        Assertions.assertEquals(response2, r!![SRV_2])
                        isPassed.set(true)
                    }
            Assertions.assertTrue(isPassed.get())
        }

        @Test
        @DisplayName("Send HTTP request to 2 service When one throws exception Then verify response received from both")
        fun testSendHttpRequestToAllServicesAndOneFailed() {
            val httpTestStep = HttpTestStep.Builder()
                    .withService(listOf(SRV_1, SRV_2))
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()

            whenever(httpChannel.send(anyOrNull())).thenReturn(HttpResponse.Builder().build())
            whenever(httpChannel2.send(anyOrNull())).thenThrow(IllegalArgumentException("Failed to send request"))

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, r, e ->
                        Assertions.assertNotNull(e)
                        Assertions.assertNull(r)
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
        }

        @Test
        @DisplayName("Send HTTP request When no services provided Then throw an error")
        fun testSendHttpRequestWhenNoServicesProvided() {
            val httpTestStep = HttpTestStep.Builder()
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, _, e ->
                        Assertions.assertEquals("No services were provided to run an HTTP request." +
                                " Registered services are: [srv1, srv2]", e!!.message)
                        isPassed.set(true)
                    }
            Assertions.assertTrue(isPassed.get())
        }

        @Test
        @DisplayName("Send HTTP request to a service which not registered Then throw an error")
        fun testSendHttpRequestToNonExistentService() {
            val httpTestStep = HttpTestStep.Builder()
                    .withService(listOf("srv3"))
                    .withUrl("/a/b/c")
                    .withMethod("POST")
                    .withName("n1")
                    .build()

            val isPassed = AtomicBoolean()
            processor!!.process(httpTestStep)
                    .subscribe { _, _, e ->
                        Assertions.assertEquals("Service 'srv3' was not registered in HTTP Processor." +
                                " Registered services are: [srv1, srv2]", e!!.message)
                        isPassed.set(true)
                    }

            Assertions.assertTrue(isPassed.get())
        }
    }

}