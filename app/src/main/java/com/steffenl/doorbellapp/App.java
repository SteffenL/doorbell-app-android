package com.steffenl.doorbellapp;

import android.app.Application;
import android.app.Notification;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import com.steffenl.doorbellapp.core.NotificationProcessor;
import com.steffenl.doorbellapp.core.config.SharedPreferencesAppConfig;

public class App extends Application {
    private SharedPreferences preferences;
    private AppContainer appContainer;

    final String CHANNEL_ID_ENFORCE_SOUND = "com.steffenl.doorbellapp.notifications.enforcesound";
    final String CHANNEL_ID_NORMAL = "com.steffenl.doorbellapp.notifications.normal";
    final String CHANNEL_ID_ALERT = "com.steffenl.doorbellapp.notifications.alert";

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        appContainer = new AppContainer(new SharedPreferencesAppConfig(preferences));

        addNotificationHandlers();
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }

    private void addNotificationHandlers() {
        final NotificationProcessor notificationProcessor = appContainer.getNotificationProcessor();

        notificationProcessor.addHandler("button_pressed", () -> {
            final boolean enforceSound = preferences.getBoolean("always_emit_notification_sound", false);

            final String channelID = enforceSound ? CHANNEL_ID_ENFORCE_SOUND : CHANNEL_ID_NORMAL;
            final String channelName = "Doorbell Notifications";

            final Uri soundURI = Uri.parse(String.format(
                    "%s://%s/raw/%s",
                    ContentResolver.SCHEME_ANDROID_RESOURCE,
                    getPackageName(),
                    "doorbell"));

            final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.deleteNotificationChannel(channelID);
                final AudioAttributes audioAttributes = new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(enforceSound ? AudioAttributes.USAGE_ALARM : AudioAttributes.USAGE_NOTIFICATION)
                        .setFlags(enforceSound ? AudioAttributes.FLAG_AUDIBILITY_ENFORCED : 0)
                        .build();
                final NotificationChannelCompat channel = new NotificationChannelCompat.Builder(channelID, NotificationManagerCompat.IMPORTANCE_HIGH)
                        .setSound(soundURI, audioAttributes)
                        .setName(channelName)
                        .setVibrationEnabled(true)
                        .setLightsEnabled(true)
                        .build();
                notificationManager.createNotificationChannel(channel);
            }

            // TODO: Clean up code

            final Notification notification = new NotificationCompat.Builder(this, channelID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.notification_ring_button_pressed_title))
                    .setContentText(getString(R.string.notification_ring_button_pressed_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setSound(soundURI, AudioManager.STREAM_ALARM)
                    .setLocalOnly(true)
                    .build();

            notificationManager.notify(1001, notification);
        });

        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        final String alarmChannelName = "Doorbell Alarms";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(CHANNEL_ID_ALERT);
            final NotificationChannelCompat channel = new NotificationChannelCompat.Builder(CHANNEL_ID_ALERT, NotificationManagerCompat.IMPORTANCE_HIGH)
                    .setName(alarmChannelName)
                    .setVibrationEnabled(true)
                    .setLightsEnabled(true)
                    .build();
            notificationManager.createNotificationChannel(channel);
        }

        notificationProcessor.addHandler("device_gone", () -> {
            final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.notification_device_gone_title))
                    .setContentText(getString(R.string.notification_device_gone_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setLocalOnly(true)
                    .build();

            notificationManager.notify(1002, notification);
        });

        notificationProcessor.addHandler("battery_level_moderate", () -> {
            final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.notification_battery_level_title))
                    .setContentText(getString(R.string.notification_battery_level_moderate_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setLocalOnly(true)
                    .build();

            notificationManager.notify(1003, notification);
        });

        notificationProcessor.addHandler("battery_level_low", () -> {
            final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.notification_battery_level_title))
                    .setContentText(getString(R.string.notification_battery_level_low_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setLocalOnly(true)
                    .build();

            notificationManager.notify(1004, notification);
        });

        notificationProcessor.addHandler("battery_level_critical", () -> {
            final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_ALERT)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(getString(R.string.notification_battery_level_title))
                    .setContentText(getString(R.string.notification_battery_level_critical_text))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setCategory(Notification.CATEGORY_ALARM)
                    .setLocalOnly(true)
                    .build();

            notificationManager.notify(1005, notification);
        });
    }
}
