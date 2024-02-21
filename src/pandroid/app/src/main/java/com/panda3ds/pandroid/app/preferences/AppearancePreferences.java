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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AppearancePreferences extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.appearance_preference, rootKey);

        setActivityTitle(R.string.appearance);

       SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
       int themeValue = sharedPreferences.getInt("theme_entry", 0);
       ListPreference listPreference = findPreference("theme");
       listPreference.setSummary(listPreference.getEntries()[themeValue]);
       listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
         @Override
         public boolean onPreferenceChange(Preference preference, Object newValue) {
         int themeValue = Integer.parseInt((String) newValue);
         GlobalConfig.set(GlobalConfig.KEY_APP_THEME, themeValue);

         // Get the index of the selected value
         int index = listPreference.findIndexOfValue((String) newValue);
        
         // Update the summary with the selected entry
         if (index >= 0) {
            listPreference.setSummary(listPreference.getEntries()[index]);
            sharedPreferences.edit().putInt("theme_entry", index).apply();
         }
         return true;
         }
       });
    }
}
