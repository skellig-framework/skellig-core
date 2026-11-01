package org.skellig.teststep.processing.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.skellig.teststep.processing.state.TestScenarioScope
import org.skellig.teststep.processing.state.TestScenarioState
import org.apache.log4j.MDC
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DisplayName("Scope in the log context")
class ScopeLogContextTest {

    private val scopeA = TestScenarioScope("scope-A", mock<TestScenarioState>())
    private val scopeB = TestScenarioScope("scope-B", mock<TestScenarioState>())

    @AfterEach
    fun cleanMdcOfTestThread() {
        MDC.remove(ScopeLogContext.KEY)
    }

    @Test
    @DisplayName("When run with scope Then scope id is in log context only while running")
    fun testRunWithScope() {
        val idInBlock = ScopeLogContext.runWith(scopeA) { ScopeLogContext.currentScopeId() }

        assertEquals("scope-A", idInBlock)
        assertNull(ScopeLogContext.currentScopeId())
    }

    @Test
    @DisplayName("When run with scope Then result of the block is returned")
    fun testRunWithScopeReturnsResult() {
        assertEquals(42, ScopeLogContext.runWith(scopeA) { 42 })
    }

    @Test
    @DisplayName("When run with scope inside another scope Then previous scope id is restored after")
    fun testRunWithScopeInsideAnotherScope() {
        val ids = mutableListOf<String?>()

        ScopeLogContext.runWith(scopeA) {
            ScopeLogContext.runWith(scopeB) { ids.add(ScopeLogContext.currentScopeId()) }
            ids.add(ScopeLogContext.currentScopeId())
        }

        assertEquals(listOf("scope-B", "scope-A"), ids)
    }

    @Test
    @DisplayName("When block fails Then scope id is removed from log context And exception is thrown")
    fun testRunWithScopeWhenBlockFails() {
        assertThrows(IllegalStateException::class.java) {
            ScopeLogContext.runWith(scopeA) { error("failed") }
        }

        assertNull(ScopeLogContext.currentScopeId())
    }

    @Test
    @DisplayName("When run without scope Then log context of the thread has no scope id while running")
    fun testRunWithoutScopeHidesScopeIdOfTheThread() {
        MDC.put(ScopeLogContext.KEY, "scope-A")

        val idInBlock = ScopeLogContext.runWith(null) { ScopeLogContext.currentScopeId() }

        assertNull(idInBlock)
        assertEquals("scope-A", ScopeLogContext.currentScopeId())
    }

    @Test
    @DisplayName("When task runs on a worker which ran a task of another scope Then it sees only its own scope id")
    fun testWorkerIsReusedByAnotherScope() {
        val executor = Executors.newSingleThreadExecutor()
        try {
            executor.submit { ScopeLogContext.runWith(scopeA) { } }.get(10, TimeUnit.SECONDS)

            val idOfScopeB = executor.submit<String?> { ScopeLogContext.runWith(scopeB) { ScopeLogContext.currentScopeId() } }
                .get(10, TimeUnit.SECONDS)
            val idOutsideOfScope = executor.submit<String?> { ScopeLogContext.currentScopeId() }.get(10, TimeUnit.SECONDS)

            assertEquals("scope-B", idOfScopeB)
            assertNull(idOutsideOfScope)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    @DisplayName("When coroutines of scope run on shared workers Then they see the scope id even if workers were created for another scope")
    fun testCoroutinesOnSharedWorkersSeeScopeId() {
        val seen = ConcurrentHashMap.newKeySet<String>()
        withCoroutineWorkersOfScope("other-scope") {
            runBlocking(ScopeLogContext.coroutineContext(scopeA)) {
                withContext(Dispatchers.Default) {
                    coroutineScope {
                        repeat(64) { launch { Thread.sleep(10); seen.add(ScopeLogContext.currentScopeId() ?: "<none>") } }
                    }
                }
            }
        }

        assertEquals(setOf("scope-A"), seen)
    }

    @Test
    @DisplayName("When coroutines of scope run Then the thread which started them keeps its own scope id")
    fun testCoroutinesDoNotChangeScopeIdOfTheStartingThread() {
        MDC.put(ScopeLogContext.KEY, "scope-B")

        runBlocking(ScopeLogContext.coroutineContext(scopeA)) {
            withContext(Dispatchers.Default) { }
        }

        assertEquals("scope-B", ScopeLogContext.currentScopeId())
    }

    @Test
    @DisplayName("When coroutines run without scope Then they see no scope id of the starting thread")
    fun testCoroutinesWithoutScopeSeeNoScopeId() {
        MDC.put(ScopeLogContext.KEY, "scope-A")

        val seen = ConcurrentHashMap.newKeySet<String>()
        runBlocking(ScopeLogContext.coroutineContext(null)) {
            withContext(Dispatchers.Default) {
                coroutineScope {
                    repeat(16) { launch { Thread.sleep(10); seen.add(ScopeLogContext.currentScopeId() ?: "<none>") } }
                }
            }
            seen.add(ScopeLogContext.currentScopeId() ?: "<none>")
        }

        assertEquals(setOf("<none>"), seen)
    }
}
