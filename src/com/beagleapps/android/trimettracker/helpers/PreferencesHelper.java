package com.beagleapps.android.trimettracker.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beagleapps.android.trimettracker.objects.Preference;
import com.beagleapps.android.trimettrackerfree.JSONReader;

public class PreferencesHelper {

	public enum SortMethods{
		VISITS(DBHelper.COL_HT_VISITS, "DESC"), 
		TIME(DBHelper.COL_HT_LAST_VISITED, "DESC"),
		STOPID(DBHelper.COL_STOPID, "ASC");

		private String columnName;
		private String sortDirection;
		
		private SortMethods(String columnName, String sortDirection){
			this.columnName = columnName;
			this.sortDirection = sortDirection;
		}
		
		public String columnName(){
			return columnName;
		}
		
		public static SortMethods get(int ordinal){
			return SortMethods.values()[ordinal];
		}

		public String sqlSort() {
			return columnName + " " + sortDirection;
		}
	};
	
	public enum DisplayChoice{
		ALL, 
		FAVORITES;
		
		public static DisplayChoice get(int ordinal){
			return DisplayChoice.values()[ordinal];
		}
	};
	
	public enum Theme{
		LIGHT, 
		DARK;
		
		public static Theme get(int ordinal){
			return Theme.values()[ordinal];
		}
	};
	
	public enum Preferences{
		SORT("sortMethod"), DISPLAY_CHOICE("displayChoice"), THEME("theme"), REFRESH_ON_UNLOCK("refreshOnUnlock");
		private String value;
		
		private Preferences(String value){
			this.value = value;
		}
		
		public String value(){
			return this.value;
		}
		
		public static Preferences get(int ordinal){
			return Preferences.values()[ordinal];
		}
	};
	
	public enum Columns{
		ID("id"), NAME("name"), VALUE("value"), DESCRIPTION("description");
		private String value;
		
		private Columns(String value){
			this.value = value;
		}
		
		public String value(){
			return this.value;
		}
	};

	static void insertDefaultPreferences(SQLiteDatabase database, Context context) {
		JSONObject jsonObject = JSONReader.getJson("preferences.json", context);
		JSONArray preferences = null;
		
		try {
			preferences = jsonObject.getJSONArray("preferences");
			
			for (int index = 0; index < preferences.length(); index++){
				String name = getString(preferences, index, "name");
				int defaultValue = getInt(preferences, index, "default");
				String description = getString(preferences, index, "description");
				
				insertPreference(database, name, defaultValue, description);
			}
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	static long insertPreference(SQLiteDatabase database, String name, int defaultValue, String description){
		ContentValues values = new ContentValues();
		values.put(DBHelper.COL_PREF_NAME, name);
		values.put(DBHelper.COL_PREF_VALUE, defaultValue);
		values.put(DBHelper.COL_PREF_DESCRIPTION, description);
		
		return database.insert(DBHelper.TABLE_PREFERENCES, null, values);
	}
	
	public static boolean updatePreference(SQLiteDatabase database, Preference preference) {
		ContentValues updateValues = new ContentValues();
		updateValues.put(DBHelper.COL_PREF_NAME, preference.name);
		updateValues.put(DBHelper.COL_PREF_VALUE, preference.value);
		updateValues.put(DBHelper.COL_PREF_DESCRIPTION, preference.description);

		return database.update(DBHelper.TABLE_PREFERENCES, updateValues, DBHelper.COL_PREF_NAME + "="
				+ "'" + preference.name + "'", null) > 0;
	}
	
	static int getInt(JSONArray array, int index, String key) throws NumberFormatException, JSONException{
		return Integer.parseInt(array.getJSONObject(index).getString(key).toString());
	}
	
	static String getString(JSONArray array, int index, String key) throws NumberFormatException, JSONException{
		return array.getJSONObject(index).getString(key).toString();
	}
	
	public static Preference getPreference(SQLiteDatabase database, PreferencesHelper.Preferences preferenceName){
		Preference preference = null;
		Cursor cursor = database.query(DBHelper.TABLE_PREFERENCES, null,
				PreferencesHelper.Columns.NAME.value() + " = '" + preferenceName.value() + "'", null, null, null, null);
		
		cursor.moveToFirst();
		
		if(cursor.getCount() > 0){
			preference = constructPreferenceFromCursor(cursor);
		}
		
		cursor.close();
		
		return preference;
	}
	
	private static Preference constructPreferenceFromCursor(Cursor cursor) {
		Preference preference = new Preference();
		cursor.moveToFirst();
		preference.description = CursorHelper.getString(cursor, PreferencesHelper.Columns.DESCRIPTION.value());
		preference.value = CursorHelper.getInt(cursor, PreferencesHelper.Columns.VALUE.value());
		preference.name = CursorHelper.getString(cursor, PreferencesHelper.Columns.NAME.value());
		
		return preference;
	}
	
	public static SortMethods getSort(SQLiteDatabase database){
		Preference preference = getPreference(database, Preferences.SORT);
		
		return SortMethods.get(preference.value);
	}
	
	public static Theme getTheme(SQLiteDatabase database){
		Preference preference = getPreference(database, Preferences.THEME);
		
		return Theme.get(preference.value);
	}
	
	public static DisplayChoice getDisplayChoice(SQLiteDatabase database){
		Preference preference = getPreference(database, Preferences.DISPLAY_CHOICE);
		
		return DisplayChoice.get(preference.value);
	}

	public static void setSort(SQLiteDatabase database, int sortMethod) {
		Preference sortPref = getPreference(database, Preferences.SORT);
		sortPref.value = sortMethod;
		
		updatePreference(database, sortPref);
	}
	
	public static void setTheme(SQLiteDatabase database, int theme) {
		Preference themePref = getPreference(database, Preferences.THEME);
		themePref.value = theme;
		
		updatePreference(database, themePref);
	}
	
	public static void setDisplayChoice(SQLiteDatabase database, int displayChoice) {
		Preference displayChoicePref = getPreference(database, Preferences.DISPLAY_CHOICE);
		displayChoicePref.value = displayChoice;
		
		updatePreference(database, displayChoicePref);
	}
	
	
}
