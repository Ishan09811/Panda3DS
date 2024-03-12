package com.panda3ds.pandroid.app.preferences;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.PandroidApplication;
import com.panda3ds.pandroid.app.base.BasePreferenceFragment;
import com.panda3ds.pandroid.app.services.LoggerService;
import com.panda3ds.pandroid.data.config.GlobalConfig;
import java.io.File;

public class AdvancedPreferences extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.advanced_preferences, rootKey);
        setActivityTitle(R.string.advanced_options);

        setItemClick("shareLog", pref -> {
            shareLogFile();
        });
        setItemClick("performanceMonitor", pref -> GlobalConfig.set(GlobalConfig.KEY_SHOW_PERFORMANCE_OVERLAY, ((SwitchPreferenceCompat) pref).isChecked()));
        setItemClick("shaderJit", pref -> GlobalConfig.set(GlobalConfig.KEY_SHADER_JIT, ((SwitchPreferenceCompat) pref).isChecked()));
        setItemClick("loggerService", pref -> {
            boolean checked = ((SwitchPreferenceCompat) pref).isChecked();
            Context ctx = PandroidApplication.getAppContext();
            if (checked) {
                findPreference("shareLog").setVisible(true);
                ctx.startService(new Intent(ctx, LoggerService.class));
            } else {
                findPreference("shareLog").setVisible(false);
                ctx.stopService(new Intent(ctx, LoggerService.class));
            }
            GlobalConfig.set(GlobalConfig.KEY_LOGGER_SERVICE, checked);
        });

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        if (GlobalConfig.get(GlobalConfig.KEY_LOGGER_SERVICE)) {
        findPreference("shareLog").setVisible(true);
        } else {
        findPreference("shareLog").setVisible(false);
        }
        ((SwitchPreferenceCompat) findPreference("performanceMonitor")).setChecked(GlobalConfig.get(GlobalConfig.KEY_SHOW_PERFORMANCE_OVERLAY));
        ((SwitchPreferenceCompat) findPreference("loggerService")).setChecked(GlobalConfig.get(GlobalConfig.KEY_LOGGER_SERVICE));
        ((SwitchPreferenceCompat) findPreference("shaderJit")).setChecked(GlobalConfig.get(GlobalConfig.KEY_SHADER_JIT));
    }

    private void shareLogFile() {
       String filePath = "/storage/emulated/0/Android/media/com.panda3ds.pandroid/logs/current.txt";
       File file = new File(filePath);

       // Check if the log file exists and then share
       if (file.exists()) {
           Uri uri = FileProvider.getUriForFile(requireContext(), "com.panda3ds.pandroid.fileprovider", file);

           Intent intent = new Intent(Intent.ACTION_SEND);
           intent.setType("text/plain");
           intent.putExtra(Intent.EXTRA_STREAM, uri);
           intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
           startActivity(Intent.createChooser(intent, "Share Log File"));
       } else {
           Toast.makeText(requireContext(), getString(R.string.no_log_file_found), Toast.LENGTH_SHORT).show();
       }
    }
}
