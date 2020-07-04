package org.skellig.task.async;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skellig.task.exception.TaskRunException;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.skellig.task.async.AsyncTaskUtils.runTaskAsync;

@DisplayName("Run async task")
class AsyncTaskUtilsTest {

    private static final String RESPONSE = "ok";

    @AfterAll
    static void afterAll() {
        AsyncTaskUtils.shutdown();
    }

    @Test
    @DisplayName("When result received within a timeout")
    void testAsyncTask() throws Exception {
        Future<String> asyncResult = runTaskAsync(() -> {
            Thread.sleep(50);
            return RESPONSE;
        });

        assertEquals(RESPONSE, asyncResult.get(500, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    void testAsyncTaskWithStopCondition() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        Future<String> asyncResult = runTaskAsync(() -> {
            countDownLatch.countDown();
            if (countDownLatch.getCount() > 1) {
                return null;
            } else {
                countDownLatch.countDown();
                return RESPONSE;
            }
        }, Objects::nonNull, 10, 1);

        countDownLatch.await(1000, TimeUnit.MILLISECONDS);

        assertEquals(0, countDownLatch.getCount());
        assertEquals(RESPONSE, asyncResult.get(10, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("When getting result from async operation not received within timeout")
    void testAsyncTaskWhenThrowException() {
        Future<String> asyncResult = runTaskAsync(() -> {
            Thread.sleep(100);
            throw new TaskRunException("oops");
        });

        assertThrows(ExecutionException.class, () -> asyncResult.get(300, TimeUnit.MILLISECONDS));
    }
}