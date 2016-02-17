package org.skellig.task.async;


import org.skellig.task.exception.TaskRunException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.skellig.task.TaskUtils.runTask;

public final class AsyncTaskUtils {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    private AsyncTaskUtils() {
    }

    public static <T> Future<T> runTaskAsync(Callable<T> task) {
        try {
            return executorService.submit(task);
        } catch (Exception e) {
            throw new TaskRunException(e.getMessage(), e);
        }
    }

    public static void runTaskAsync(Runnable task) {
        try {
            executorService.submit(task);
        } catch (Exception e) {
            throw new TaskRunException(e.getMessage(), e);
        }
    }

    public static <T> Future<T> runTaskAsync(Callable<T> task, Predicate<T> stopCondition, int timeout) {
        return runTaskAsync(() -> runTask(task, stopCondition, 0, timeout));
    }

    public static <T> Future<T> runTaskAsync(Callable<T> task, Predicate<T> stopCondition,
                                             int delay, int timeout) {
        return runTaskAsync(() -> runTask(task, stopCondition, delay, timeout));
    }

    public static void shutdown() {
        executorService.shutdown();
        try {
            executorService.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new TaskRunException("Waiting for termination of the running async tasks took too long", e);
        }
    }

}
