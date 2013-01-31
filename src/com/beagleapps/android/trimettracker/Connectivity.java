package com.beagleapps.android.trimettracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;
import com.beagleapps.android.trimettrackerfree.R;

public class Connectivity {
//	public static boolean checkForInternetConnection(Context context) 
//	{
//		boolean connected = false;
//		ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//	    if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || 
//	            connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED) {
//	        //we are connected to a network
//	        connected = true;
//	    }
//	    else
//	        connected = false;
//	    return connected;
//	}
	
	public static boolean checkForInternetConnection(Context context) 
	{
		ConnectivityManager connectivityManager 
        = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;
	}
	
	public static void showErrorToast(Context context)
	{
		Toast.makeText(context, R.string.noInternetError, Toast.LENGTH_SHORT).show();
	}
	
}
