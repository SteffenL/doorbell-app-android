package com.steffenl.doorbellapp;

import com.steffenl.doorbellapp.core.APIClient;
import com.steffenl.doorbellapp.core.NotificationProcessor;
import com.steffenl.doorbellapp.core.config.AppConfig;
import com.steffenl.doorbellapp.core.service.AppService;
import com.steffenl.doorbellapp.core.service.tasks.TaskExecutor;

import java.util.concurrent.Executors;

public class AppContainer {
    private final AppConfig appConfig;
    private final AppService appService;
    private final NotificationProcessor notificationProcessor;

    public AppContainer(final AppConfig appConfig) {
        this.appConfig = appConfig;
        final APIClient apiClient = new APIClient(appConfig.getAPIConfig());
        final TaskExecutor taskExecutor = new TaskExecutor(Executors.newSingleThreadExecutor());
        appService = new AppService(apiClient, taskExecutor);
        notificationProcessor = new NotificationProcessor();
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public AppService getAppService() {
        return appService;
    }

    public NotificationProcessor getNotificationProcessor() {
        return notificationProcessor;
    }
}
