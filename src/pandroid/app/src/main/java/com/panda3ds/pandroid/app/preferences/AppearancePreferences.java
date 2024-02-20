package com.panda3ds.pandroid.app.preferences;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.BaseActivity;
import com.panda3ds.pandroid.app.base.BasePreferenceFragment;
import com.panda3ds.pandroid.data.config.GlobalConfig;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;

public class AppearancePreferences extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.appearance_preference, rootKey);

        setActivityTitle(R.string.appearance);

       ListPreference listPreference = findPreference("theme");
       listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue) {
         GlobalConfig.set(GlobalConfig.KEY_APP_THEME, (Integer) newValue);
         return true;
         }
       });
    }
}
