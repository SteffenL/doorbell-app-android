package com.steffenl.doorbellapp.core.service;

import com.steffenl.doorbellapp.core.APIClient;
import com.steffenl.doorbellapp.core.service.tasks.TaskCommand;
import com.steffenl.doorbellapp.core.service.tasks.TaskExecutor;

public class AppService {
    private final TaskExecutor executor;
    private final APIClient apiClient;

    public AppService(final APIClient apiClient, final TaskExecutor executor) {
        this.apiClient = apiClient;
        this.executor = executor;
    }

    public void uploadDeviceToken(final String token, final TaskCommand.Callback callback) {
        executor.submit(() -> apiClient.postDeviceToken(token), callback);
    }

    public void ring(final TaskCommand.Callback callback) {
        executor.submit(() -> apiClient.ring(), callback);
    }
}
