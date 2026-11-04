package org.skellig.runner

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Records the JUnit events of a run, in the order they are fired, with the name of the thread which fired them.
 */
class RecordingRunListener : RunListener() {

    enum class Type { SUITE_STARTED, SUITE_FINISHED, TEST_STARTED, TEST_FINISHED, TEST_FAILURE, TEST_IGNORED }

    /**
     * @property description What the event is about. The class name of a step is the name of its scenario.
     */
    class Event(val type: Type, val description: Description, val thread: String)

    val events = CopyOnWriteArrayList<Event>()

    override fun testSuiteStarted(description: Description) = record(Type.SUITE_STARTED, description)

    override fun testSuiteFinished(description: Description) = record(Type.SUITE_FINISHED, description)

    override fun testStarted(description: Description) = record(Type.TEST_STARTED, description)

    override fun testFinished(description: Description) = record(Type.TEST_FINISHED, description)

    override fun testFailure(failure: Failure) = record(Type.TEST_FAILURE, failure.description)

    override fun testIgnored(description: Description) = record(Type.TEST_IGNORED, description)

    private fun record(type: Type, description: Description) {
        events.add(Event(type, description, Thread.currentThread().name))
    }
}
