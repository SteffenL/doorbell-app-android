package com.steffenl.doorbellapp.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.steffenl.doorbellapp.R;

public class AppSettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
