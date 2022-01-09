package com.steffenl.doorbellapp.core.config;

import android.content.SharedPreferences;

public class SharedPreferencesAppConfig implements AppConfig {
    private final APIConfig apiConfig;
    private final UIConfig uiConfig;

    public SharedPreferencesAppConfig(final SharedPreferences preferences) {
        apiConfig = new SharedPreferencesAPIConfig(preferences);
        uiConfig = new SharedPreferencesUIConfig(preferences);
    }

    @Override
    public APIConfig getAPIConfig() {
        return apiConfig;
    }

    @Override
    public UIConfig getUIConfig() {
        return uiConfig;
    }
}
