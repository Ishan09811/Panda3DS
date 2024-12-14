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
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(navigationBar, navController);
            navigationBar.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
                @Override
                public void onNavigationItemReselected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.gamesFragment:
                            navController.navigate(R.id.gamesFragment);
                            break;
                        case R.id.searchFragment:
                            navController.navigate(R.id.searchFragment);
                            break;
                        case R.id.settingsFragment:
                            navController.navigate(R.id.settingsFragment);
                            break;
                    }
                }
            });
        }   
    }
}
