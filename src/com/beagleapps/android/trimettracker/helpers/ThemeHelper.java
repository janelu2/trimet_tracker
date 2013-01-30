package com.beagleapps.android.trimettracker.helpers;

import android.app.Activity;

import com.beagleapps.android.trimettracker.adapters.DBAdapter;

public class ThemeHelper {

	public static void setThemeOnCreate(Activity activity, DBAdapter dbHelper) {
		switch(PreferencesHelper.getTheme(dbHelper.getDatabase())){
		case LIGHT:
			//activity.setTheme();
			break;
		case DARK:
			//activity.setTheme(R.style.Theme_Dark);
			break;
		default:
			break;
		}
	}

}
