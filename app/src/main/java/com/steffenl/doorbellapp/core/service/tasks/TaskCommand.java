package com.steffenl.doorbellapp.core.service.tasks;

public interface TaskCommand {
    void execute() throws Exception;

    interface Callback {
        void completed();
        void errored(Exception exception);

        class Discarding implements TaskCommand.Callback {
            @Override
            public void completed() {
                // Do nothing
            }

            @Override
            public void errored(final Exception exception) {
                // Do nothing
            }
        }
    }
}
