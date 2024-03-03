package org.skellig.feature.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.skellig.feature.exception.SkelligClassInstanceRegistryException
import org.skellig.feature.hook.annotation.AfterAll
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeAll
import org.skellig.feature.hook.annotation.BeforeTestScenario

class DefaultSkelligTestHooksRegistryTest {

    @Test
    fun testRegisterHooks() {
        val classInstanceRegistry = mutableMapOf<Class<*>, Any>()
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"), classInstanceRegistry)

        val hooks = registry.getHooks()
        val beforeScenarioHooks = hooks.filter { it.type == BeforeTestScenario::class.java }
        val afterScenarioHook = hooks.find { it.type == AfterTestScenario::class.java }
        val beforeAllHook = hooks.find { it.type == BeforeAll::class.java }
        val afterAllHook = hooks.find { it.type == AfterAll::class.java }

        val methods = HookClass::class.java.methods
        val instance = classInstanceRegistry[HookClass::class.java]
        assertAll(
            { assertEquals(1, classInstanceRegistry.size) },

            { assertTrue(beforeScenarioHooks.any { h -> h.method == methods.find { it.name == "beforeScenario" } }) },
            { assertTrue(beforeScenarioHooks.any { h -> h.method == methods.find { it.name == "beforeScenario2" } }) },

            { assertEquals(methods.find { it.name == "afterScenario" }, afterScenarioHook?.method) },
            { assertEquals(instance, afterScenarioHook?.instance) },
            { assertEquals(2, afterScenarioHook?.order) },
            { assertEquals(setOf("T3"), afterScenarioHook?.tags) },

            { assertEquals(methods.find { it.name == "beforeAll" }, beforeAllHook?.method) },
            { assertEquals(instance, beforeAllHook?.instance) },
            { assertEquals(3, beforeAllHook?.order) },
            { assertEquals(setOf("T4"), beforeAllHook?.tags) },

            { assertEquals(methods.find { it.name == "afterAll" }, afterAllHook?.method) },
            { assertEquals(instance, afterAllHook?.instance) },
            { assertEquals(4, afterAllHook?.order) },
            { assertEquals(setOf("T5"), afterAllHook?.tags) },
        )
    }

    @Test
    fun testRegisterHooksAndGetByTags() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"), mutableMapOf())

        val beforeHooks = registry.getByTags(BeforeTestScenario::class.java, setOf("T0", "T2"))
        val afterHooks = registry.getByTags(AfterTestScenario::class.java, setOf("T1", "T2", "T3", "T6", "T10"))

        assertAll(
            { assertEquals(2, beforeHooks.size) },
            { assertTrue(beforeHooks.any { h -> h.method.name == "beforeScenario" }) },
            { assertTrue(beforeHooks.any { h -> h.method.name == "beforeScenario2" }) },

            { assertEquals(2, afterHooks.size) },
            { assertTrue(afterHooks.find { it.method.name == "afterScenario" } != null) },
            { assertTrue(afterHooks.find { it.method.name == "afterScenario2" } != null) }
        )
    }

    @Test
    fun testRegisterHooksAndGetByTagsWithEmptyTags() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"), mutableMapOf())

        assertEquals(1, registry.getByTags(BeforeTestScenario::class.java, emptySet()).size)
        assertEquals(1, registry.getByTags(BeforeTestScenario::class.java, null).size)
        assertEquals("beforeScenario2", registry.getByTags(BeforeTestScenario::class.java, null).first().method.name)

        assertEquals(0, registry.getByTags(AfterAll::class.java, emptySet()).size)
        assertEquals(0, registry.getByTags(AfterAll::class.java, null).size)
    }

    @Test
    fun testRegisterHooksAndGetByTagsWithIgnoredPrivateMethod() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"), mutableMapOf())

        assertTrue(registry.getByTags(AfterTestScenario::class.java, emptySet()).isEmpty())
    }

    @Test
    fun testRegisterHooksWhereClassWithArgsConstructor() {
        val ex = assertThrows<SkelligClassInstanceRegistryException> {
            DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.invalid"), mutableMapOf())
        }
        assertEquals(
            "Failed to instantiate class 'org.skellig.feature.invalid.HookClassWithArgsConstructor'. " +
                    "The hook class must have default constructor.", ex.message
        )
    }

    class HookClass {
        @BeforeAll(tags = ["T4"], order = 3)
        fun beforeAll() {

        }

        @AfterAll(tags = ["T5"], order = 4)
        fun afterAll() {

        }

        @AfterTestScenario(tags = ["T3"], order = 2)
        fun afterScenario() {

        }

        @BeforeTestScenario
        fun beforeScenario2() {

        }

        @BeforeTestScenario(tags = ["T1", "T2"], order = 1)
        fun beforeScenario() {

        }

        @AfterTestScenario(tags = ["T6"])
        fun afterScenario2() {
            throw RuntimeException("unexpected error")
        }

        @AfterTestScenario
        private fun afterScenario3() {
        }
    }


}