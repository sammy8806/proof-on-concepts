package de.sammy8806.tests.testasynctasks;

import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
public class JobMeta<T> {

    @Getter
    protected String id;

    @Getter
    protected Instant creationTime;

    protected JobTask<T> executorMethod;

    protected Future<T> future;

    public AsyncBackend.Status getStatus() {
        // Try to get the result from the Task to determine the status.
        try {
            future.get(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException e) {
            // Return FAILED if the result got an exception
            log.trace("Found Stacktrace for Task {}: {}", id, e.getMessage(), e);
            return AsyncBackend.Status.FAILED;
        } catch (TimeoutException e) {
            // Return STARTED if the timeout to get the data times out
            return AsyncBackend.Status.STARTED;
        }

        if (future.isDone()) {
            return AsyncBackend.Status.FINISHED;
        }

        return AsyncBackend.Status.STARTED;
    }

    public T getResult() throws InterruptedException, ExecutionException {
        T data;

        try {
            data = future.get();
        } catch (ExecutionException e) {
            data = null;
            log.info("Task {} has failed with exception: {}", id, e.getMessage(), e);
            throw e;
        }

        return data;
    }

    public boolean isDone() {
        return future.isDone();
    }

    @FunctionalInterface
    interface JobTask<T> extends Supplier<T> {
    }

}
