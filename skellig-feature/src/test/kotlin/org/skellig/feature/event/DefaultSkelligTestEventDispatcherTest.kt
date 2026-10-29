package org.skellig.feature.event

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class DefaultSkelligTestEventDispatcherTest {

    @Test
    fun `events dispatched from several threads reach every plugin`() {
        val dispatcher = DefaultSkelligTestEventDispatcher()
        val threads = 8
        val eventsPerThread = 500
        val handlerCounters = List(3) { AtomicInteger() }
        handlerCounters.forEach { counter -> dispatcher.register(TestRunFinishedEvent::class) { counter.incrementAndGet() } }

        runConcurrently(threads) { repeat(eventsPerThread) { dispatcher.dispatch(TestRunFinishedEvent()) } }

        handlerCounters.forEach { assertEquals(threads * eventsPerThread, it.get()) }
    }

    @Test
    fun `handlers registered while events are dispatched do not break dispatching`() {
        val dispatcher = DefaultSkelligTestEventDispatcher()
        val errors = ConcurrentLinkedQueue<Throwable>()
        val registered = AtomicInteger()

        runConcurrently(8) { index ->
            try {
                repeat(300) {
                    if (index % 2 == 0) dispatcher.register(TestRunFinishedEvent::class) { registered.incrementAndGet() }
                    else dispatcher.dispatch(TestRunFinishedEvent())
                }
            } catch (e: Throwable) {
                errors.add(e)
            }
        }

        assertTrue(errors.isEmpty(), "Unexpected errors: $errors")
        var total = 0
        dispatcher.register(TestRunFinishedEvent::class) { total++ }
        dispatcher.dispatch(TestRunFinishedEvent())
        assertEquals(1, total)
    }

    private fun runConcurrently(threads: Int, action: (Int) -> Unit) {
        val pool = Executors.newFixedThreadPool(threads)
        val start = CountDownLatch(1)
        val futures = (0 until threads).map { index -> pool.submit { start.await(); action(index) } }
        start.countDown()
        futures.forEach { it.get(30, TimeUnit.SECONDS) }
        pool.shutdownNow()
    }
}
