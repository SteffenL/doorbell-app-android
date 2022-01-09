package com.steffenl.doorbellapp.core.service.tasks;

import java.util.concurrent.ExecutorService;

public class TaskExecutor {
    private final ExecutorService executor;

    public TaskExecutor(final ExecutorService executor) {
        this.executor = executor;
    }

    public <T> void submit(final TaskQuery<T> task, final TaskQuery.Callback<T> callback) {
        executor.submit(() -> {
            try {
                final T result = task.execute();
                callback.completed(result);
            } catch (final Exception e) {
                callback.errored(e);
            }
        });
    }

    public void submit(final TaskCommand task, final TaskCommand.Callback callback) {
        executor.submit(() -> {
            try {
                task.execute();
                callback.completed();
            } catch (final Exception e) {
                callback.errored(e);
            }
        });
    }
}
