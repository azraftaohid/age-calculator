package thegoodkid.aetate.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import thegoodkid.aetate.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preference_settings, rootKey);
    }
}
