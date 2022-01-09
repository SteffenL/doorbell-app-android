package com.steffenl.doorbellapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;
import com.steffenl.doorbellapp.App;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.R;
import com.steffenl.doorbellapp.core.AppNotification;
import com.steffenl.doorbellapp.core.service.tasks.TaskCommand;
import com.steffenl.doorbellapp.fragments.MainFragment;
import com.steffenl.doorbellapp.viewmodels.MainViewModel;
import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URI;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
                                                               SharedPreferences.OnSharedPreferenceChangeListener {
    private Socket socket = null;
    private SharedPreferences preferences;
    private AppContainer appContainer;
    private MainViewModel mainViewModel;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkSystemFeatures();

        appContainer = ((App) getApplication()).getAppContainer();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.main_container, new MainFragment())
                    .commit();
        }

        mainViewModel = new ViewModelProvider(this, MainViewModel.Factory.getInstance(appContainer)).get(MainViewModel.class);

        final boolean showRingButton = appContainer.getAppConfig().getUIConfig().getShowRingButton();
        mainViewModel.setShowRingButton(showRingButton);

        mainViewModel.getToastMessage().observe(this, resourceID -> {
            Toast.makeText(this, resourceID, Toast.LENGTH_SHORT).show();
        });

        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                return;
            }

            final String token = task.getResult();
            appContainer.getAppService().uploadDeviceToken(token, new TaskCommand.Callback.Discarding());
        });

        final String apiEndpoint = appContainer.getAppConfig().getAPIConfig().getEndpoint();
        if (apiEndpoint != null && socket == null) {
            mainViewModel.setStatusText(R.string.status_connecting);

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
                    runOnUiThread(() -> {
                        mainViewModel.setStatusText(R.string.status_connected);
                    });
                    mainViewModel.refresh();
                });
                socket.on(Socket.EVENT_DISCONNECT, args -> {
                    runOnUiThread(() -> {
                        mainViewModel.setStatusText(R.string.status_disconnected);
                    });
                });
                socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                    runOnUiThread(() -> {
                        mainViewModel.setStatusText(R.string.status_connection_error);
                    });
                });
                socket.connect();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.settings) {
            final Intent intent = new Intent(this, AppSettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.help) {
            Toast.makeText(this, R.string.feature_not_yet_implemented, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void checkSystemFeatures() {
        final PackageManager packageManager = getPackageManager();

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Toast.makeText(this, R.string.wifi_direct_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public boolean onPreferenceStartFragment(final PreferenceFragmentCompat caller, final Preference pref) {
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
        if (key.equals("show_ring_button")) {
            final boolean showRingButton = appContainer.getAppConfig().getUIConfig().getShowRingButton();
            mainViewModel.setShowRingButton(showRingButton);
        }
    }
}
