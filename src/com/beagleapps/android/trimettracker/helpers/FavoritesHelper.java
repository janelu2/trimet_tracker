package com.beagleapps.android.trimettracker.helpers;

import com.beagleapps.android.trimettracker.objects.Favorite;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class FavoritesHelper {
	public static Cursor fetchAllFavorites(SQLiteDatabase database) {
		Cursor cursor = database.query(DBHelper.TABLE_FAVORITES, new String[] { DBHelper.COL_STOPID,
				DBHelper.COL_DESCRIPTION, DBHelper.COL_DIRECTION, DBHelper.COL_ROUTES }, null, null, null,
				null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		return cursor;
	}
	
	public static Favorite constructFavoriteFromCursor(Cursor cursor) {
		Favorite fav = new Favorite();
		fav.setDescription(cursor.getString(cursor.getColumnIndex(DBHelper.COL_DESCRIPTION)));
		fav.setStopID(cursor.getInt(cursor.getColumnIndex(DBHelper.COL_STOPID)));
		fav.setDirection(cursor.getString(cursor.getColumnIndex(DBHelper.COL_DIRECTION)));
		fav.setRoutes(cursor.getString(cursor.getColumnIndex(DBHelper.COL_ROUTES)));
		return fav;
	}
	
	/**
	 * Checks if a stopID is in the DB
	 */
	public static boolean checkForFavorite(SQLiteDatabase database, int stopID) {
		Cursor cursor = database.query(DBHelper.TABLE_FAVORITES, new String[] {DBHelper.COL_STOPID},
				DBHelper.COL_STOPID + "=" + stopID, null, null, null, null);
		
		int count = cursor.getCount();
		cursor.close();
		return count > 0;
	}
	
	public static long createFavorite(SQLiteDatabase database, Favorite newFavorite) {
		ContentValues initialValues = createFavoriteContentValues(newFavorite);

		return database.insert(DBHelper.TABLE_FAVORITES, null, initialValues);
	}


	public static boolean updateFavorite(SQLiteDatabase database, Favorite favorite) {
		ContentValues updateValues = createFavoriteContentValues(favorite);

		return database.update(DBHelper.TABLE_FAVORITES, updateValues, DBHelper.COL_STOPID + "="
				+ favorite.getStopID(), null) > 0;
	}

	public static boolean deleteFavorite(SQLiteDatabase database, int stopID) {
		return database.delete(DBHelper.TABLE_FAVORITES, DBHelper.COL_STOPID + "=" + 
				stopID, null) > 0;
	}

	/**
	 * Return a Cursor positioned at the defined todo
	 */
	public static Cursor fetchFavorite(SQLiteDatabase database, long stopID) throws SQLException {
		Cursor mCursor = database.query(true, DBHelper.TABLE_FAVORITES, new String[] {
				DBHelper.COL_STOPID, DBHelper.COL_DESCRIPTION, DBHelper.COL_DIRECTION, DBHelper.COL_ROUTES },
				DBHelper.COL_STOPID + "=" + stopID, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	private static ContentValues createFavoriteContentValues(Favorite fav) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COL_DESCRIPTION, fav.getDescription());
		values.put(DBHelper.COL_STOPID, fav.getStopID());
		values.put(DBHelper.COL_DIRECTION, fav.getDirection());
		values.put(DBHelper.COL_ROUTES, fav.getRoutes());
		return values;
	}
}
