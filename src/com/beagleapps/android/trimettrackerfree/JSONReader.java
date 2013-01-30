package com.beagleapps.android.trimettrackerfree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;

public class JSONReader {

	public static String readAsset(String asset, Context context) {
		return readAssetBase(asset, context.getResources().getAssets());
	}
	
	public static String readAsset(String asset, Activity activity) {
		return readAssetBase(asset, activity.getAssets());
	}
	
	public static String readAssetBase(String asset, AssetManager assetManager) {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(assetManager.open(asset)));
			
			String line;
			StringBuilder buffer = new StringBuilder();
			
			while ((line = in.readLine()) != null) {
				buffer.append(line).append('\n');
			}
			
			return buffer.toString();
			
		} 
		
		catch (IOException e) {
			return "";
		}
		
		finally {
			try {
				in.close();
			} catch (IOException e) {
				return "";
			}
		}
	}

	public static JSONObject getJson(String asset, Context context) {
		try {
			return new JSONObject(readAsset(asset, context));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}
}
