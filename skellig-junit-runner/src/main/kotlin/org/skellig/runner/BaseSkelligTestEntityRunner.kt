package org.skellig.runner

import org.junit.runner.notification.RunNotifier
import org.junit.runners.ParentRunner
import org.skellig.feature.SkelligTestEntity
import org.skellig.feature.hook.SkelligHookRunner
import org.skellig.runner.junit.report.TestStepLogger
import org.skellig.runner.junit.report.model.HookReportDetails

abstract class BaseSkelligTestEntityRunner<T : SkelligTestEntity>(
    protected val testEntity: SkelligTestEntity,
    protected val hookRunner: SkelligHookRunner,
    protected val testStepLogger: TestStepLogger,
    protected val beforeHookType: Class<out Annotation>,
    protected val afterHookType: Class<out Annotation>,
) : ParentRunner<T>(testEntity::class.java), SkelligTestEntity {

    protected val beforeHookReportDetails = mutableListOf<HookReportDetails>()
    protected val afterHookReportDetails = mutableListOf<HookReportDetails>()

    override fun getEntityName(): String = testEntity.getEntityName()

    override fun getEntityTags(): Set<String>?  = testEntity.getEntityTags()

    override fun getName(): String = getEntityName()

    override fun run(notifier: RunNotifier) {
        try {
            runBeforeHooks()
            super.run(notifier)
        } finally {
            runAfterHooks()
        }
    }

    protected fun runAfterHooks() {
        hookRunner.run(testEntity.getEntityTags(), beforeHookType) { name, e, duration ->
            afterHookReportDetails.add(createHookReportDetails(name, e, duration))
        }
    }

    protected fun runBeforeHooks() {
        hookRunner.run(testEntity.getEntityTags(), afterHookType) { name, e, duration ->
            beforeHookReportDetails.add(createHookReportDetails(name, e, duration))
        }
    }

    private fun createHookReportDetails(name: String, e: Throwable?, duration: Long) =
        HookReportDetails(name, e?.message, testStepLogger.getLogsAndClean(), duration)

}