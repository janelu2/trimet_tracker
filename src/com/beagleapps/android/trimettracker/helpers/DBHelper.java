package com.beagleapps.android.trimettracker.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {
	private Context mContext;

	// Database fields
	// Favorites Table
	public static final String COL_DESCRIPTION = "description";
	public static final String COL_STOPID = "stopid";
	public static final String COL_DIRECTION = "direction";
	public static final String COL_ROUTES = "routes";
	
	// Version Table
	public static final String COL_VERSION_ID = "version_number";
	public static final String COL_VERSION_NUMBER = "version_id";
	
	// Stop History Table
	public static final String COL_HT_STOPID = "stopid";
	public static final String COL_HT_VISITS = "visits";
	public static final String COL_HT_LAST_VISITED= "last_visited";
	public static final String COL_HT_ORDER= "orderNumber";
	
	// Preferences Table
	public static final String COL_PREF_ID = "id";
	public static final String COL_PREF_NAME = "name";
	public static final String COL_PREF_VALUE = "value";
	public static final String COL_PREF_DESCRIPTION= "description";
	
	
	// Table names
	public static final String TABLE_FAVORITES = "favorites";
	public static final String TABLE_VERSION = "version_table";
	public static final String TABLE_STOP_HISTORY = "stop_history";
	public static final String TABLE_PREFERENCES = "preferences";
		
	public static final String DATABASE_NAME = "applicationdata";

	public static final int DATABASE_VERSION = 3;

	// Database creation sql statement
	private static final String TABLE_FAVORITE_CREATE = "create table "+ TABLE_FAVORITES+ " ("+COL_STOPID+" integer primary key, "
			+ COL_DESCRIPTION + " text not null, "+COL_ROUTES+" text not null, "+COL_DIRECTION+" text not null);";
	private static final String TABLE_VERSION_CREATE= "create table "+TABLE_VERSION+" ("+COL_VERSION_ID+" integer primary key, "
			+ COL_VERSION_NUMBER+" text not null);";
	private static final String TABLE_STOP_HISTORY_CREATE= "create table "+TABLE_STOP_HISTORY+" ("+COL_HT_STOPID+" integer primary key, "
			+ COL_DESCRIPTION + " text not null, "+COL_ROUTES+" text not null, "+COL_DIRECTION+" text not null, "
			+ COL_HT_VISITS+" INTEGER DEFAULT 0, "  + COL_HT_ORDER + " INTEGER NOT NULL, "
			+ COL_HT_LAST_VISITED + " INTEGER DEFAULT NULL);";
	private static final String TABLE_PREFERENCES_CREATE= "create table "+TABLE_PREFERENCES+" ("+COL_PREF_ID+" integer primary key, "
			+ COL_PREF_NAME+" text not null, " + COL_PREF_DESCRIPTION+" text not null, " + COL_PREF_VALUE+" INTEGER NOT NULL);";
	

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
		
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(TABLE_FAVORITE_CREATE);
		database.execSQL(TABLE_VERSION_CREATE);
		database.execSQL(TABLE_STOP_HISTORY_CREATE);
		database.execSQL(TABLE_PREFERENCES_CREATE);
		PreferencesHelper.insertDefaultPreferences(database, mContext);
	}

	// Method is called during an update of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ".");

		migrateDatabase(database, oldVersion, newVersion);
		
	}

	public void migrateDatabase(SQLiteDatabase database, int oldVersion, int newVersion) {
		// Loops through each version of the database and migrates it to the next version
		// This way, changes in the database only need to care about how the previous version
		// was set up.
		for(int version = oldVersion; version < newVersion; version++){
			switch(version){
				case 1:
					// Version 1 is ancient and not installed on any devices
					break;
				case 2:
					// Version 2-3: Adds tables: stop_history, preferences
					database.execSQL(TABLE_STOP_HISTORY_CREATE);
					database.execSQL(TABLE_PREFERENCES_CREATE);
					PreferencesHelper.insertDefaultPreferences(database, mContext);
					// Add their current favorites to the history
					HistoryHelper.addFavoritesToHistory(database);
					break;
				case 3:
					// Version 3-4: Not used yet
					break;
				case 4:
					// Version 4-5: Not used yet
					break;
				default:
					// If the number is not found, something must be terribly wrong.
					// Drop everything, recreate the database
					//dropAllTables(database);
					//onCreate(database);
				break;
			}
		}
	}
	

	public void dropAllTables(SQLiteDatabase database){
		// TODO
	}
}