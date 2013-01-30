package com.beagleapps.android.trimettracker.helpers;

import android.database.Cursor;

public class CursorHelper {
	public static int getInt(Cursor cursor, String columnName){
		return cursor.getInt(cursor.getColumnIndex(columnName));
	}
	
	public static String getString(Cursor cursor, String columnName){
		return cursor.getString(cursor.getColumnIndex(columnName));
	}
}
