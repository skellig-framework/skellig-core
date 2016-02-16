package org.skellig.task;


import org.skellig.task.exception.TaskRunException;

import java.util.concurrent.Callable;
import java.util.function.Predicate;

public final class TaskUtils {

    private static final double MS_CONSTANT = 1000;

    private TaskUtils() {
    }

    public static <T> T runTask(Callable<T> task, Predicate<T> stopCondition, int delay, int timeout) throws TaskRunException {
        return runTaskUntil(task, stopCondition, delay, timeout, null);
    }

    private static <T> T runTaskUntil(Callable<T> task, Predicate<T> stopCondition, int delay, double timeout, T previousResult) throws TaskRunException {
        if (timeout >= 0) {
            long startTime = System.currentTimeMillis();
            T result;
            try {
                result = task.call();
            } catch (Exception ex) {
                throw new TaskRunException(ex.getMessage(), ex);
            }
            int totalExecutionTime = (int) (System.currentTimeMillis() - startTime);

            if (stopCondition.test(result)) {
                return result;
            } else {
                if (delay > 0) {
                    delay(delay);
                }
                return runTaskUntil(task, stopCondition, delay, timeout - delay/MS_CONSTANT - totalExecutionTime, result);
            }
        }
        return previousResult;
    }

    private static void delay(int idleMilliseconds) {
        try {
            Thread.sleep(idleMilliseconds);
        } catch (InterruptedException ignored) {
        }
    }
}
