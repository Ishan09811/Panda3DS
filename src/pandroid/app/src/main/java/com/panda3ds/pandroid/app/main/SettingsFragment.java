package com.panda3ds.pandroid.app.main;

import android.content.Context;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import androidx.annotation.Nullable;

import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.PandroidApplication;
import com.panda3ds.pandroid.app.PreferenceActivity;
import com.panda3ds.pandroid.app.base.BasePreferenceFragment;
import com.panda3ds.pandroid.app.preferences.GeneralPreferences;
import com.panda3ds.pandroid.app.preferences.AdvancedPreferences;
import com.panda3ds.pandroid.app.preferences.InputPreferences;
import java.io.File;

public class SettingsFragment extends BasePreferenceFragment {
    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.start_preferences, rootKey);
        findPreference("application").setSummary(getVersionName());
        setItemClick("input", (item) -> PreferenceActivity.launch(requireContext(), InputPreferences.class));
        setItemClick("general", (item)-> PreferenceActivity.launch(requireContext(), GeneralPreferences.class));
        setItemClick("advanced", (item)-> PreferenceActivity.launch(requireContext(), AdvancedPreferences.class));
        setItemClick("share_log", (item)-> {
            // Share Log File
            shareLogFile();
        });
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
           Toast.makeText(requireContext(), "No log file found", Toast.LENGTH_SHORT).show();
       }
   }

    private String getVersionName() {
        try {
            Context context = PandroidApplication.getAppContext();
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            return "Error: Unknown version";
        }
    }
}
