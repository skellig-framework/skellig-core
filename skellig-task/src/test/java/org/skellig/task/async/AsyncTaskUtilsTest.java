package org.skellig.task.async;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
        AsyncResult<String> asyncResult = runTaskAsync(() -> {
            Thread.sleep(50);
            return RESPONSE;
        }, 1);

        assertEquals(RESPONSE, asyncResult.get(500, TimeUnit.MILLISECONDS));
    }

    @Test
    @DisplayName("When result received within a timeout And stop condition satisfied")
    void testAsyncTaskWithStopCondition() throws Exception {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        AsyncResult<String> asyncResult = runTaskAsync(() -> {
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
    void testAsyncTaskWhenFailedInTimeout() {
        AsyncResult<String> asyncResult = runTaskAsync(() -> {
            Thread.sleep(50);
            return RESPONSE;
        }, 0);

        assertThrows(TimeoutException.class, () -> asyncResult.get(10, TimeUnit.MILLISECONDS));
    }
}