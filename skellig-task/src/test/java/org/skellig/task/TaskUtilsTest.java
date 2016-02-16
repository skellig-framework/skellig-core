package org.skellig.task;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.skellig.task.TaskUtils.runTask;

@DisplayName("Run task")
class TaskUtilsTest {

    private static final String RESPONSE = "ok";

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    void testRunTaskWithStopCondition() {
        AtomicInteger atomicInteger = new AtomicInteger(0);

        String response = runTask(() -> {
            if (atomicInteger.incrementAndGet() < 7) {
                return null;
            } else {
                return RESPONSE;
            }
        }, Objects::nonNull, 10, 1);

        assertEquals(7, atomicInteger.get());
        assertEquals(RESPONSE, response);
    }

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    void testRunTaskWithStopConditionWhenTimedOut() {
        assertNull(TaskUtils.<String>runTask(() -> null, Objects::nonNull, 100, 1));
    }

}