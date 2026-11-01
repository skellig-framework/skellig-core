package org.skellig.feature.hook

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.skellig.feature.exception.SkelligClassInstanceRegistryException
import org.skellig.feature.hook.annotation.AfterTestFeature
import org.skellig.feature.hook.annotation.AfterTestScenario
import org.skellig.feature.hook.annotation.BeforeTestFeature
import org.skellig.feature.hook.annotation.BeforeTestScenario
import org.skellig.feature.lazy.LazyHooks

class DefaultSkelligTestHooksRegistryTest {

    @Test
    fun testRegisterHooks() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"))

        val hooks = registry.getHooks()
        val beforeScenarioHooks = hooks.filter { it.type == BeforeTestScenario::class.java }
        val afterScenarioHook = hooks.find { it.type == AfterTestScenario::class.java }
        val beforeTestFeatureHook = hooks.find { it.type == BeforeTestFeature::class.java }
        val afterTestFeatureHook = hooks.find { it.type == AfterTestFeature::class.java }

        val methods = HookClass::class.java.methods
        assertAll(
            { assertTrue(beforeScenarioHooks.any { h -> h.method == methods.find { it.name == "beforeScenario" } }) },
            { assertTrue(beforeScenarioHooks.any { h -> h.method == methods.find { it.name == "beforeScenario2" } }) },

            { assertEquals(methods.find { it.name == "afterScenario" }, afterScenarioHook?.method) },
            { assertEquals(HookClass::class.java, afterScenarioHook?.hookClass) },
            { assertEquals(2, afterScenarioHook?.order) },
            { assertEquals(setOf("@T3"), afterScenarioHook?.tags) },

            { assertEquals(methods.find { it.name == "beforeAll" }, beforeTestFeatureHook?.method) },
            { assertEquals(HookClass::class.java, beforeTestFeatureHook?.hookClass) },
            { assertEquals(3, beforeTestFeatureHook?.order) },
            { assertEquals(setOf("@T4"), beforeTestFeatureHook?.tags) },

            { assertEquals(methods.find { it.name == "afterAll" }, afterTestFeatureHook?.method) },
            { assertEquals(HookClass::class.java, afterTestFeatureHook?.hookClass) },
            { assertEquals(4, afterTestFeatureHook?.order) },
            { assertEquals(setOf("@T5"), afterTestFeatureHook?.tags) },
        )
    }

    @Test
    fun testRegisterHooksAndGetByTags() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"))

        val beforeHooks = registry.getByTags(BeforeTestScenario::class.java, setOf("@T0", "@T2"))
        val afterHooks = registry.getByTags(AfterTestScenario::class.java, setOf("@T1", "@T2", "@T3", "@T6", "@T10"))

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
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"))

        assertEquals(1, registry.getByTags(BeforeTestScenario::class.java, emptySet()).size)
        assertEquals(1, registry.getByTags(BeforeTestScenario::class.java, null).size)
        assertEquals("beforeScenario2", registry.getByTags(BeforeTestScenario::class.java, null).first().method.name)

        assertEquals(0, registry.getByTags(AfterTestFeature::class.java, emptySet()).size)
        assertEquals(0, registry.getByTags(AfterTestFeature::class.java, null).size)
    }

    @Test
    fun testRegisterHooksAndGetByTagsWithIgnoredPrivateMethod() {
        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.hook"))

        assertTrue(registry.getByTags(AfterTestScenario::class.java, emptySet()).isEmpty())
    }

    @Test
    fun testHookClassesAreNotInstantiatedAtStartup() {
        LazyHooks.created.set(0)

        val registry = DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.lazy"))

        assertAll(
            { assertEquals(1, registry.getHooks().size) },
            { assertEquals(LazyHooks::class.java, registry.getHooks().single().hookClass) },
            { assertEquals(0, LazyHooks.created.get()) },
        )
    }

    @Test
    fun testRegisterHooksWhereClassWithArgsConstructor() {
        val ex = assertThrows<SkelligClassInstanceRegistryException> {
            DefaultSkelligTestHooksRegistry(listOf("org.skellig.feature.invalid"))
        }
        assertEquals(
            "Failed to instantiate class 'org.skellig.feature.invalid.HookClassWithArgsConstructor'. " +
                    "The hook class must have default constructor.", ex.message
        )
    }

    class HookClass {
        @BeforeTestFeature(tags = ["@T4"], order = 3)
        fun beforeAll() {

        }

        @AfterTestFeature(tags = ["@T5"], order = 4)
        fun afterAll() {

        }

        @AfterTestScenario(tags = ["@T3"], order = 2)
        fun afterScenario() {

        }

        @BeforeTestScenario
        fun beforeScenario2() {

        }

        @BeforeTestScenario(tags = ["@T1", "@T2"], order = 1)
        fun beforeScenario() {

        }

        @AfterTestScenario(tags = ["@T6"])
        fun afterScenario2() {
            throw RuntimeException("unexpected error")
        }

        @AfterTestScenario
        private fun afterScenario3() {
        }
    }


}