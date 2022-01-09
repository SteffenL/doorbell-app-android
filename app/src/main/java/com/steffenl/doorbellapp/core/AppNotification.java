package com.steffenl.doorbellapp.core;

public class AppNotification {
    private final String eventId;
    private final String eventName;

    public AppNotification(final String eventId, final String eventName) {
        this.eventId = eventId;
        this.eventName = eventName;
    }

    public String getId() {
        return eventId;
    }

    public String getName() {
        return eventName;
    }
}
