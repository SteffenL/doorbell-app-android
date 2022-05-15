package com.steffenl.doorbellapp.activities;

import static com.steffenl.doorbellapp.services.DoorbellService.ACTION_STATUS_CHANGED;
import static com.steffenl.doorbellapp.services.DoorbellService.EXTRA_STATUS_TEXT;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.steffenl.doorbellapp.App;
import com.steffenl.doorbellapp.AppContainer;
import com.steffenl.doorbellapp.R;
import com.steffenl.doorbellapp.fragments.MainFragment;
import com.steffenl.doorbellapp.services.DoorbellService;
import com.steffenl.doorbellapp.viewmodels.MainViewModel;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback,
                                                               SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences preferences;
    private AppContainer appContainer;
    private MainViewModel mainViewModel;
    private StatusChangeReceiver statusChangeReceiver;

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
        statusChangeReceiver = new StatusChangeReceiver(mainViewModel);
        registerReceiver(statusChangeReceiver, new IntentFilter(ACTION_STATUS_CHANGED));
        DoorbellService.start(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(statusChangeReceiver);
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainViewModel.refresh();
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

    public static class StatusChangeReceiver extends BroadcastReceiver {
        private final MainViewModel mainViewModel;

        public StatusChangeReceiver(final MainViewModel mainViewModel) {
            this.mainViewModel = mainViewModel;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int resourceID = intent.getIntExtra(EXTRA_STATUS_TEXT, R.string.status_idle);
            mainViewModel.setStatusText(resourceID);
        }
    }
}
