package org.skellig.task;


import org.skellig.task.exception.TaskRunException;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

public final class TaskUtils {

    private static final double MS_CONSTANT = 1000;

    private TaskUtils() {
    }

    public static <T> T runTask(Callable<T> task, Predicate<T> stopCondition, int delay, int timeout) throws TaskRunException {
        return runTaskUntil(task, stopCondition, delay, timeout);
    }

    private static <T> T runTaskUntil(Callable<T> task, Predicate<T> stopCondition, int delay, double timeout) throws TaskRunException {
        T result = null;

        while (timeout > 0) {
            long startTime = System.currentTimeMillis();
            try {
                result = task.call();
            } catch (Exception ex) {
                throw new TaskRunException(ex.getMessage(), ex);
            }
            int totalExecutionTime = (int) (System.currentTimeMillis() - startTime);

            if (stopCondition.test(result)) {
                timeout = 0;
            } else {
                if (delay > 0) {
                    delay(delay);
                }
                timeout = timeout - delay / MS_CONSTANT - totalExecutionTime;
            }
        }
        return result;
    }

    private static void delay(int idleMilliseconds) {
        try {
            Thread.sleep(idleMilliseconds);
        } catch (InterruptedException ignored) {
        }
    }
}
