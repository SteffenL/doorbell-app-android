package com.steffenl.doorbellapp.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.google.firebase.messaging.FirebaseMessaging;
import com.steffenl.doorbellapp.App;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.R;
import com.steffenl.doorbellapp.core.AppNotification;
import com.steffenl.doorbellapp.core.service.tasks.TaskCommand;

import java.net.URI;

import io.socket.client.IO;
import io.socket.client.Socket;

public class DoorbellService extends Service {
    public static final String ACTION_STATUS_CHANGED = DoorbellService.class.getName() + ".STATUS_CHANGED";
    public static final String EXTRA_STATUS_TEXT = "STATUS_TEXT";

    private AppContainer appContainer;
    private Socket socket = null;

    public DoorbellService() {
    }

    @Override
    public void onDestroy() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        appContainer = ((App) getApplication()).getAppContainer();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            final String token = task.getResult();
            appContainer.getAppService().uploadDeviceToken(token, new TaskCommand.Callback.Discarding());
        });

        final String apiEndpoint = appContainer.getAppConfig().getAPIConfig().getEndpoint();
        if (apiEndpoint != null && socket == null) {
            notifyStatusChanged(R.string.status_connecting);

            URI apiEndpointUri = null;
            try {
                apiEndpointUri = URI.create(apiEndpoint);
            } catch (final IllegalArgumentException e) {
                // Ignore
            }

            if (apiEndpointUri != null) {
                final IO.Options options = IO.Options.builder()
                        .build();
                socket = IO.socket(apiEndpointUri, options);
                socket.on("doorbell", args -> {
                    final String id = (String) args[0];
                    final String name = (String) args[1];
                    final AppNotification notification = new AppNotification(id, name);
                    appContainer.getNotificationProcessor().submit(notification);
                });
                socket.on(Socket.EVENT_CONNECT, args -> {
                    notifyStatusChanged(R.string.status_connected);
                });
                socket.on(Socket.EVENT_DISCONNECT, args -> {
                    notifyStatusChanged(R.string.status_disconnected);
                });
                socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                    notifyStatusChanged(R.string.status_connection_error);
                });
                socket.connect();
            }
        }
    }

    private void notifyStatusChanged(final int resourceID) {
        final Intent intent = new Intent(ACTION_STATUS_CHANGED);
        intent.putExtra(EXTRA_STATUS_TEXT, resourceID);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        return START_STICKY;
    }

    public static void start(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, DoorbellService.class));
        } else {
            context.startService(new Intent(context, DoorbellService.class));
        }
    }
}