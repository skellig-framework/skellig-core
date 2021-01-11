package org.skellig.teststep.processor.http

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.skellig.teststep.processing.converter.TestStepResultConverter
import org.skellig.teststep.processing.processor.TestStepProcessor
import org.skellig.teststep.processing.state.TestScenarioState
import org.skellig.teststep.processing.validation.TestStepResultValidator
import org.skellig.teststep.processor.http.model.HttpResponse
import org.skellig.teststep.processor.http.model.HttpTestStep
import java.util.concurrent.atomic.AtomicBoolean

class HttpTestStepProcessorTest {

    companion object {
        private const val SRV_2 = "srv2"
        private const val SRV_1 = "srv1"
    }

    private var processor: TestStepProcessor<HttpTestStep>? = null
    private var httpChannel = Mockito.mock(HttpChannel::class.java)
    private var httpChannel2 = Mockito.mock(HttpChannel::class.java)

    @BeforeEach
    fun setUp() {
        val channels = mapOf(
                Pair("srv1", httpChannel),
                Pair("srv2", httpChannel2))

        processor = HttpTestStepProcessor(channels, Mockito.mock(TestScenarioState::class.java),
                Mockito.mock(TestStepResultValidator::class.java), Mockito.mock(TestStepResultConverter::class.java))
    }

    @Test
    @DisplayName("Send HTTP request to a single service Then verify response received un-grouped")
    fun testSendHttpRequest() {
        val httpTestStep = HttpTestStep.Builder()
                .withService(listOf(SRV_1))
                .withUrl("/a/b/c")
                .withMethod("POST")
                .withName("n1")
                .build() as HttpTestStep

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
    @DisplayName("Send HTTP request to 2 service Then verify response received from both")
    fun testSendHttpRequestToAllServices() {
        val httpTestStep = HttpTestStep.Builder()
                .withService(listOf(SRV_1, SRV_2))
                .withUrl("/a/b/c")
                .withMethod("POST")
                .withName("n1")
                .build() as HttpTestStep
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
    @DisplayName("Send HTTP request When no services provided Then throw an error")
    fun testSendHttpRequestWhenNoServicesProvided() {
        val httpTestStep = HttpTestStep.Builder()
                .withUrl("/a/b/c")
                .withMethod("POST")
                .withName("n1")
                .build() as HttpTestStep

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
                .build() as HttpTestStep

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