package com.steffenl.doorbellapp.core.config;

import android.content.SharedPreferences;

public class SharedPreferencesUIConfig implements UIConfig {
    private final SharedPreferences preferences;

    public SharedPreferencesUIConfig(final SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public boolean getShowRingButton() {
        return preferences.getBoolean("show_ring_button", false);
    }
}
