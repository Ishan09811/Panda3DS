package com.panda3ds.pandroid.app;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.panda3ds.pandroid.AlberDriver;
import com.panda3ds.pandroid.R;
import com.panda3ds.pandroid.app.services.LoggerService;
import com.panda3ds.pandroid.data.config.GlobalConfig;
import com.panda3ds.pandroid.input.InputMap;
import com.panda3ds.pandroid.utils.GameUtils;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.DynamicColorsOptions;


public class PandroidApplication extends Application {
	private static Context appContext;

	@Override
	public void onCreate() {
		super.onCreate();
		appContext = this;

		GlobalConfig.initialize();
		GameUtils.initialize();
		InputMap.initialize();
		AlberDriver.Setup();

               if (GlobalConfig.get(GlobalConfig.KEY_APP_THEME) == GlobalConfig.THEME_DYNAMIC) {
		DynamicColorsOptions dynamicColorsOptions = new DynamicColorsOptions.Builder()
                  .build();

                DynamicColors.applyToActivitiesIfAvailable(this, dynamicColorsOptions);
	       }       


		if (GlobalConfig.get(GlobalConfig.KEY_LOGGER_SERVICE)) {
			startService(new Intent(this, LoggerService.class));
		}
	}

	public static int getThemeId() {
		switch (GlobalConfig.get(GlobalConfig.KEY_APP_THEME)) {
			case GlobalConfig.THEME_LIGHT:
				return R.style.Theme_Pandroid_Light;
			case GlobalConfig.THEME_DARK:
				return R.style.Theme_Pandroid_Dark;
			case GlobalConfig.THEME_BLACK:
				return R.style.Theme_Pandroid_Black;
		}

		return R.style.Theme_Pandroid;
	}

	public static boolean isDarkMode() {
		switch (GlobalConfig.get(GlobalConfig.KEY_APP_THEME)) {
			case GlobalConfig.THEME_DARK:
			case GlobalConfig.THEME_BLACK:
				return true;
			case GlobalConfig.THEME_LIGHT:
				return false;
		}
        
		Resources res = Resources.getSystem();
		int nightFlags = res.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		return nightFlags == Configuration.UI_MODE_NIGHT_YES;
	}

	public static Context getAppContext() { return appContext; }
}
