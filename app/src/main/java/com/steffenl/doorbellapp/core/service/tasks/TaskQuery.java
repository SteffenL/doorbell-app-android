package com.steffenl.doorbellapp.core.service.tasks;

public interface TaskQuery<T> {
    T execute() throws Exception;

    interface Callback<T> {
        void completed(T result);
        void errored(Exception exception);

        class Discarding<T> implements TaskQuery.Callback<T> {
            @Override
            public void completed(final T result) {
                // Do nothing
            }

            @Override
            public void errored(final Exception exception) {
                // Do nothing
            }
        }
    }
}
