package com.panda3ds.pandroid.app;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.navigation.NavigationBarView;
import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.editor.CodeEditorActivity;
import com.panda3ds.pandroid.app.main.GamesFragment;
import com.panda3ds.pandroid.app.main.SearchFragment;
import com.panda3ds.pandroid.app.main.SettingsFragment;

public class MainActivity extends BaseActivity implements NavigationBarView.OnItemSelectedListener {
    private static final int PICK_ROM = 2;
    private static final int PERMISSION_REQUEST_CODE = 3;

    private final GamesFragment gamesFragment = new GamesFragment();
    private final SearchFragment searchFragment = new SearchFragment();
    private final SettingsFragment settingsFragment = new SettingsFragment();

    // Declare the ActivityResultLauncher for MANAGE_EXTERNAL_STORAGE
    private final ActivityResultLauncher<String> requestManageStorageLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted, no specific logic needed
                } else {
                    // Permission is denied, handle accordingly
                    // You may inform the user or take appropriate actions
                    finishAffinity(); // Finish the activity and its associated activities
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        NavigationBarView bar = findViewById(R.id.navigation);
        bar.setOnItemSelectedListener(this);
        bar.postDelayed(() -> bar.setSelectedItemId(bar.getSelectedItemId()), 5);

        // Check and request MANAGE_EXTERNAL_STORAGE permission
        checkAndRequestManageStoragePermission();
    }

    private void checkAndRequestManageStoragePermission() {
        // Check if the app has the MANAGE_EXTERNAL_STORAGE permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && !Environment.isExternalStorageManager()) {
            // Request the MANAGE_EXTERNAL_STORAGE permission
            requestManageStorageLauncher.launch(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        } else {
            // Permission is already granted or not needed on lower Android versions
            // Proceed with your logic or inform the user
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment;
        if (id == R.id.games) {
            fragment = gamesFragment;
        } else if (id == R.id.search) {
            fragment = searchFragment;
        } else if (id == R.id.settings) {
            fragment = settingsFragment;
        } else {
            return false;
        }

        manager.beginTransaction().replace(R.id.fragment_container, fragment).commitNow();
        return true;
    }
}
