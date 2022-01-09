package com.steffenl.doorbellapp.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationProcessor {
    private static final String TAG = NotificationProcessor.class.getName();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Queue<AppNotification> notifications = new LinkedList<>();
    private final Map<String, String> idToNameMap = new HashMap<>();
    private final Map<String, List<NotificationHandler>> nameToHandlerMap = new HashMap<>();

    public void addHandler(final String name, final NotificationHandler handler) {
        synchronized (this) {
            List<NotificationHandler> handlers = nameToHandlerMap.get(name);

            if (handlers == null) {
                handlers = new ArrayList<>();
            }

            handlers.add(handler);
            nameToHandlerMap.put(name, handlers);
        }
    }

    public void submit(final AppNotification notification) {
        synchronized (this) {
            if (idToNameMap.containsKey(notification.getId())) {
                return;
            }

            notifications.add(notification);

            executorService.submit(() -> {
                synchronized (this) {
                    final AppNotification notification_ = notifications.remove();

                    if (!idToNameMap.containsKey(notification_.getId())) {
                        idToNameMap.put(notification_.getId(), notification_.getName());
                        executorService.submit(() -> this.handle(notification_));
                    }
                }
            });
        }
    }

    private void handle(final AppNotification notification) {
        synchronized (this) {
            final List<NotificationHandler> handlers = nameToHandlerMap.get(notification.getName());
            if (handlers == null) {
                return;
            }

            for (final NotificationHandler handler : handlers) {
                executorService.submit(handler::execute);
            }
        }
    }
}
