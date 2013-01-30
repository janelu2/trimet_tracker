package com.beagleapps.android.trimettracker.adapters;

import com.beagleapps.android.trimettracker.helpers.CursorHelper;
import com.beagleapps.android.trimettracker.helpers.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBAdapter {

	// Database fields
	public static final String NO_VERSION_FOUND = "No Version Found";
	private Context mContext;
	private SQLiteDatabase mDatabase;
	private DBHelper mDbHelper;

	public DBAdapter(Context context) {
		this.mContext = context;
	}

	public DBAdapter open() throws SQLException {
		mDbHelper = new DBHelper(mContext);
		mDatabase = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}
	
	public SQLiteDatabase getDatabase() {
		return mDatabase;
	}

	//=====================================================
	//
	// Version Functions
	//
	//=====================================================
	
	/**
	 * Returns Version Number
	 */
	public String fetchVersion() throws SQLException {
		Cursor cursor = mDatabase.query(true, DBHelper.TABLE_VERSION, new String[] {
				DBHelper.COL_VERSION_NUMBER},
				null, null, null, null, null, null);
		if (cursor != null && cursor.getCount() > 0) {
			cursor.moveToLast();
			String versionNumber = CursorHelper.getString(cursor, DBHelper.COL_VERSION_NUMBER);
			cursor.close();
			return versionNumber;
		}
		else{
			return NO_VERSION_FOUND;
		}
		
	}


	public long setCurrentVersion(String currentVersion) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COL_VERSION_NUMBER, currentVersion);
		
		return mDatabase.insert(DBHelper.TABLE_VERSION, null, values);
	}
	
	//=====================================================
	//
	// Preferences Functions
	//
	//=====================================================
	
	
}

