package com.steffenl.doorbellapp.services;

import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.steffenl.doorbellapp.App;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.core.AppNotification;
import com.steffenl.doorbellapp.core.service.tasks.TaskCommand;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        final String id = remoteMessage.getData().get("id");
        final String name = remoteMessage.getData().get("name");
        final AppNotification appNotification = new AppNotification(id, name);

        final AppContainer appContainer = ((App) getApplication()).getAppContainer();
        appContainer.getNotificationProcessor().submit(appNotification);
    }

    @Override
    public void onNewToken(@NonNull final String token) {
        final AppContainer appContainer = ((App) getApplication()).getAppContainer();
        appContainer.getAppService().uploadDeviceToken(token, new TaskCommand.Callback.Discarding());
    }
}
