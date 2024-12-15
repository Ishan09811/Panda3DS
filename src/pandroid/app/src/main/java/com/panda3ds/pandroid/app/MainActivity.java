package com.panda3ds.pandroid.app;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationBarView;
import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.BaseActivity;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavigationBarView navigationBar = findViewById(R.id.navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(navigationBar, navController);
        navigationBar.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.games) {
                    navController.navigate(R.id.gamesFragment);
                } else if (itemId == R.id.search) {
                    navController.navigate(R.id.searchFragment);
                } else if (itemId == R.id.settings) {
                    navController.navigate(R.id.settingsFragment);
                }       
            }
        });   
    }
}
