package com.panda3ds.pandroid.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.NavController;
import androidx.navigation.ui.setupWithNavController;

import com.google.android.material.navigation.NavigationBarView;
import com.panda3ds.pandroid.R;

public class MainActivity extends BaseActivity implements NavigationBarView.OnItemSelectedListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);

	NavigationBarView navigationBar = findViewById(R.id.navigation);
        NavHostFragment navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_container);
        navigationBar.setupWithNavController(navHostFragment.navController);
    }
}
