package com.beagleapps.android.trimettracker;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;

public class ManifestHelper {
	public static String getCurrentVersion(Activity activity) {
		try {
			return activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName;
			
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return "Unable to find version";
		}
	}
}
