package com.steffenl.doorbellapp.core.config;

import android.content.SharedPreferences;

public class SharedPreferencesAPIConfig implements APIConfig {
    private final SharedPreferences preferences;

    public SharedPreferencesAPIConfig(final SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public String getEndpoint() {
        String endpoint = preferences.getString("api_endpoint", null);
        if (endpoint != null) {
            final boolean secure = isSecure();
            final String scheme = secure ? "https://" : "http://";
            endpoint = scheme + endpoint;
        }
        return endpoint;
    }

    @Override
    public boolean isSecure() {
        return preferences.getBoolean("api_endpoint_secure", true);
    }
}
