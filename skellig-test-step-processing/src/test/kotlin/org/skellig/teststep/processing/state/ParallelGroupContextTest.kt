package org.skellig.teststep.processing.state

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class ParallelGroupContextTest {

    @BeforeEach
    fun setUp() {
        SlowStepDefs.created.set(0)
    }

    @Test
    fun testHoldsKey() {
        assertAll(
            { assertEquals("g1", ParallelGroupContext("g1").key) },
            { assertEquals("default", ParallelGroupContext.DEFAULT_KEY) },
        )
    }

    @Test
    fun testPrintsAsShortParallelGroupKey() {
        assertEquals("parallelGroup(g1)", ParallelGroupContext("g1").toString())
    }

    @Test
    fun testCreatesInstanceOnFirstUseOnly() {
        val context = ParallelGroupContext("g1")
        assertEquals(0, SlowStepDefs.created.get())

        context.instanceOf(SlowStepDefs::class.java)

        assertEquals(1, SlowStepDefs.created.get())
    }

    @Test
    fun testOneContextAlwaysReturnsTheSameInstance() {
        val context = ParallelGroupContext("g1")

        val first = context.instanceOf(CounterStepDefs::class.java)
        first.count++
        val second = context.instanceOf(CounterStepDefs::class.java)

        assertAll(
            { assertSame(first, second) },
            { assertEquals(1, second.count) },
        )
    }

    @Test
    fun testTwoContextsGetDifferentInstancesOfTheSameClass() {
        val context1 = ParallelGroupContext("g1")
        val context2 = ParallelGroupContext("g2")

        val instance1 = context1.instanceOf(CounterStepDefs::class.java)
        instance1.count = 5
        val instance2 = context2.instanceOf(CounterStepDefs::class.java)

        assertAll(
            { assertNotSame(instance1, instance2) },
            { assertEquals(5, instance1.count) },
            { assertEquals(0, instance2.count) },
        )
    }

    @Test
    fun testDifferentClassesGetDifferentInstances() {
        val context = ParallelGroupContext("g1")

        assertNotSame(context.instanceOf(CounterStepDefs::class.java), context.instanceOf(SlowStepDefs::class.java))
    }

    @Test
    fun testStepsRacingToCreateTheSameClassGetOneInstance() {
        val threads = 8
        val context = ParallelGroupContext("g1")
        val ready = CountDownLatch(threads)
        val go = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(threads)
        try {
            val instances = (1..threads).map {
                pool.submit<Any> {
                    ready.countDown()
                    go.await()
                    context.instanceOf(SlowStepDefs::class.java)
                }
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS))
            go.countDown()

            val results = instances.map { it.get(10, TimeUnit.SECONDS) }

            assertAll(
                { assertEquals(1, SlowStepDefs.created.get()) },
                { assertEquals(1, results.map { System.identityHashCode(it) }.toSet().size) },
            )
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun testInitializerCanCreateAnotherClassOfTheSameContext() {
        lateinit var context: ParallelGroupContext
        var other: Any? = null
        context = ParallelGroupContext("g1") {
            if (it is SlowStepDefs) other = context.instanceOf(CounterStepDefs::class.java)
        }

        context.instanceOf(SlowStepDefs::class.java)

        assertSame(context.instanceOf(CounterStepDefs::class.java), other)
    }

    @Test
    fun testInitializerRunsOnceForEachCreatedInstance() {
        val initialized = mutableListOf<Any>()
        val context = ParallelGroupContext("g1") { initialized.add(it) }

        val first = context.instanceOf(CounterStepDefs::class.java)
        context.instanceOf(CounterStepDefs::class.java)
        val second = context.instanceOf(SlowStepDefs::class.java)

        assertEquals(listOf(first, second), initialized)
    }

    @Test
    fun testClosingTheContextDropsItsInstances() {
        val context = ParallelGroupContext("g1")
        val before = context.instanceOf(CounterStepDefs::class.java)

        context.close()
        val after = context.instanceOf(CounterStepDefs::class.java)

        assertNotSame(before, after)
    }

    @Test
    fun testClosingOneContextKeepsInstancesOfAnother() {
        val context1 = ParallelGroupContext("g1")
        val context2 = ParallelGroupContext("g2")
        val instance2 = context2.instanceOf(CounterStepDefs::class.java)
        context1.instanceOf(CounterStepDefs::class.java)

        context1.close()

        assertSame(instance2, context2.instanceOf(CounterStepDefs::class.java))
    }

    @Test
    fun testFailsWhenClassHasNoNoArgumentConstructor() {
        val ex = assertThrows<IllegalStateException> {
            ParallelGroupContext("g1").instanceOf(NoDefaultConstructorStepDefs::class.java)
        }

        assertEquals(
            "Failed to create an instance of class '${NoDefaultConstructorStepDefs::class.java.name}' in parallel group 'g1'." +
                    " The class must have a public constructor without arguments", ex.message
        )
    }

    @Test
    fun testFailsWhenNoArgumentConstructorIsNotPublic() {
        val ex = assertThrows<IllegalStateException> {
            ParallelGroupContext("g1").instanceOf(PrivateConstructorStepDefs::class.java)
        }

        assertTrue(ex.message!!.contains("The class must have a public constructor without arguments"), ex.message)
    }

    @Test
    fun testFailsWhenClassIsAbstract() {
        val ex = assertThrows<IllegalStateException> {
            ParallelGroupContext("g1").instanceOf(AbstractStepDefs::class.java)
        }

        assertTrue(ex.message!!.contains("The class must have a public constructor without arguments"), ex.message)
    }

    @Test
    fun testFailureOfConstructorIsReportedWithItsCauseAndTheNextUseTriesAgain() {
        val context = ParallelGroupContext("g1")
        FlakyStepDefs.failures = 1

        val ex = assertThrows<IllegalStateException> { context.instanceOf(FlakyStepDefs::class.java) }
        val instance = context.instanceOf(FlakyStepDefs::class.java)

        assertAll(
            { assertEquals("Failed to create an instance of class '${FlakyStepDefs::class.java.name}' in parallel group 'g1'", ex.message) },
            { assertEquals("constructor failed", ex.cause?.message) },
            { assertNotNull(instance) },
        )
    }

    @Test
    fun testRequireNoArgConstructorAcceptsPublicConstructor() {
        assertDoesNotThrow { ParallelGroupContext.requireNoArgConstructor(CounterStepDefs::class.java) }
    }

    @Test
    fun testRequireNoArgConstructorRejectsClassWithoutIt() {
        val ex = assertThrows<NoSuchMethodException> {
            ParallelGroupContext.requireNoArgConstructor(NoDefaultConstructorStepDefs::class.java)
        }

        assertTrue(ex.message!!.contains(NoDefaultConstructorStepDefs::class.java.name), ex.message)
    }

    @Test
    fun testRequireNoArgConstructorRejectsAbstractClass() {
        assertThrows<NoSuchMethodException> {
            ParallelGroupContext.requireNoArgConstructor(AbstractStepDefs::class.java)
        }
    }

    @Test
    fun testRequireNoArgConstructorRejectsNonPublicConstructor() {
        assertThrows<NoSuchMethodException> {
            ParallelGroupContext.requireNoArgConstructor(PrivateConstructorStepDefs::class.java)
        }
    }
}

class CounterStepDefs {
    var count = 0
}

class SlowStepDefs {
    init {
        // gives a racing thread plenty of time to create its own instance if the context allowed it
        Thread.sleep(100)
        created.incrementAndGet()
    }

    companion object {
        val created = AtomicInteger()
    }
}

class NoDefaultConstructorStepDefs(@Suppress("unused") val data: String)

class PrivateConstructorStepDefs private constructor()

abstract class AbstractStepDefs

class FlakyStepDefs {
    init {
        if (failures-- > 0) throw RuntimeException("constructor failed")
    }

    companion object {
        var failures = 0
    }
}
