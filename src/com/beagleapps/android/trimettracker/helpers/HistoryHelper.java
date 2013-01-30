package com.beagleapps.android.trimettracker.helpers;

import java.util.Date;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.beagleapps.android.trimettracker.helpers.PreferencesHelper.DisplayChoice;
import com.beagleapps.android.trimettracker.helpers.PreferencesHelper.SortMethods;
import com.beagleapps.android.trimettracker.objects.Favorite;
import com.beagleapps.android.trimettracker.objects.HistoryEntry;

public class HistoryHelper {
	public static int oneHour = 60*60;
	
	// For testing
	//public static int timeLimit = 1;
	public static int timeLimit = oneHour;
	
	
	public static boolean isOutOfDate(int lastVisited){
		int age = getCurrentTime() - lastVisited;
		
		return age >= timeLimit;
	}
	
	public static int getCurrentTime() {
		return (int) ((new Date().getTime())/1000);
	}
	
	public static void addFavoritesToHistory(SQLiteDatabase database){
		Cursor favoritesCursor = FavoritesHelper.fetchAllFavorites(database);
		
		favoritesCursor.moveToFirst();

		while (!favoritesCursor.isAfterLast()){
			Favorite favorite = FavoritesHelper.constructFavoriteFromCursor(favoritesCursor); 
			addFavoriteToHistory(database, favorite);
			favoritesCursor.moveToNext();
		}
		
		favoritesCursor.close();
	}
	
	static long addFavoriteToHistory(SQLiteDatabase database, Favorite favorite){
		ContentValues values = new ContentValues();
		values.put(DBHelper.COL_HT_STOPID, favorite.getStopID());
		values.put(DBHelper.COL_HT_LAST_VISITED, HistoryHelper.getCurrentTime());
		values.put(DBHelper.COL_HT_VISITS, 1);
		values.put(DBHelper.COL_DESCRIPTION, favorite.getDescription());
		values.put(DBHelper.COL_DIRECTION, favorite.getDirection());
		values.put(DBHelper.COL_ROUTES, favorite.getRoutes());
		values.put(DBHelper.COL_HT_ORDER, getNewOrderValue(database));
		
		return database.insert(DBHelper.TABLE_STOP_HISTORY, null, values);
	}
	
	// Manually implement the auto-increment of the order column
	private static int getNewOrderValue(SQLiteDatabase database) {
		String query = "SELECT (MAX(IFNULL("+DBHelper.COL_HT_ORDER+", 0)) + 1) as \"newMax\" FROM "+DBHelper.TABLE_STOP_HISTORY+";";
		Cursor cursor = database.rawQuery(query, null);
		cursor.moveToFirst();
		int newMax = CursorHelper.getInt(cursor, "newMax");
		cursor.close();
		return newMax;
	}

	// This is the landing function that is called from elsewhere. 
		// Calls either updateHistoryEntry or createHistoryEntry depending on if an entry exists
		public static void updateHistory(SQLiteDatabase database, HistoryEntry entry) {
			if(checkForHistoryEntry(database, entry.getStopID())){
				updateHistoryEntry(database, entry);

			}
			else{
				// No entry, create one
				createHistoryEntry(database, entry);
			}
			
		}
		
		public static boolean deleteHistoryEntry(SQLiteDatabase database, int stopID) {
			return database.delete(DBHelper.TABLE_STOP_HISTORY, DBHelper.COL_STOPID + "=" + 
					stopID, null) > 0;
			
		}
		
		public static long createHistoryEntry(SQLiteDatabase database, HistoryEntry newEntry) {
			HistoryEntry entry = newEntry;
			entry.setOrder(getNewOrderValue(database));
			ContentValues initialValues = createHistoryContentValues(newEntry);

			return database.insert(DBHelper.TABLE_STOP_HISTORY, null, initialValues);
		}
		
		public static boolean updateHistoryEntry(SQLiteDatabase database, HistoryEntry entry) {
			ContentValues updateValues = createHistoryContentValues(entry);

			return database.update(DBHelper.TABLE_STOP_HISTORY, updateValues, DBHelper.COL_HT_STOPID + "="
					+ entry.getStopID(), null) > 0;
		}
		
		private static ContentValues createHistoryContentValues(HistoryEntry entry) {
			ContentValues values = new ContentValues();
			values.put(DBHelper.COL_DESCRIPTION, entry.getDescription());
			values.put(DBHelper.COL_HT_STOPID, entry.getStopID());
			values.put(DBHelper.COL_DIRECTION, entry.getDirection());
			values.put(DBHelper.COL_ROUTES, entry.getRoutes());
			values.put(DBHelper.COL_HT_VISITS, entry.getVisits());
			values.put(DBHelper.COL_HT_LAST_VISITED, entry.getLastVisited());
			values.put(DBHelper.COL_HT_ORDER, entry.getOrder());
			return values;
		}
		
		public Cursor fetchAllHistoryEntries(SQLiteDatabase database) {
			Cursor mCursor = database.query(DBHelper.TABLE_STOP_HISTORY, new String[] { DBHelper.COL_HT_STOPID,
					DBHelper.COL_DESCRIPTION, DBHelper.COL_DIRECTION, DBHelper.COL_ROUTES, 
					DBHelper.COL_HT_LAST_VISITED, DBHelper.COL_HT_VISITS}, null, null, null,
					null, null);
			
			if (mCursor != null) {
				mCursor.moveToFirst();
			}
			return mCursor;
		}
		
		/**
		 * Returns the history entry for a given stop id
		 */
		public static HistoryEntry getHistoryEntry(SQLiteDatabase database, int stopID) {
			HistoryEntry entry = null;
			
			Cursor cursor = database.query(DBHelper.TABLE_STOP_HISTORY, null,
					DBHelper.COL_HT_STOPID + "=" + stopID, null, null, null, null);
			
			if(cursor.getCount() == 1){
				cursor.moveToFirst();
				entry = constructHistoryFromCursor(cursor);
			}
			
			cursor.close();
			
			return entry;
		}
		
		public static boolean checkForHistoryEntry(SQLiteDatabase database, int stopID){
			Cursor cursor = database.query(DBHelper.TABLE_STOP_HISTORY, new String[] {DBHelper.COL_HT_STOPID},
					DBHelper.COL_HT_STOPID + "=" + stopID, null, null, null, null);
			
			int count = cursor.getCount();
			cursor.close();
			
			return count > 0;
		}
		
		public static HistoryEntry constructHistoryFromCursor(Cursor cursor) {
			HistoryEntry entry = new HistoryEntry();
			//cursor.moveToFirst();
			entry.setDescription(CursorHelper.getString(cursor, DBHelper.COL_DESCRIPTION));
			entry.setStopID(CursorHelper.getInt(cursor, DBHelper.COL_HT_STOPID));
			entry.setDirection(CursorHelper.getString(cursor, DBHelper.COL_DIRECTION));
			entry.setRoutes(CursorHelper.getString(cursor, DBHelper.COL_ROUTES));
			entry.setLastVisited(CursorHelper.getInt(cursor, DBHelper.COL_HT_LAST_VISITED));
			entry.setVisits(CursorHelper.getInt(cursor, DBHelper.COL_HT_VISITS));
			
			return entry;
		}

		public static Cursor fetchStopsUsingPrefs(SQLiteDatabase database) {
			SortMethods sortMethod = PreferencesHelper.getSort(database);
			DisplayChoice displayChoice = PreferencesHelper.getDisplayChoice(database);
			
			Cursor cursor = null;
			
			if(displayChoice == DisplayChoice.ALL){
				cursor = database.query(DBHelper.TABLE_STOP_HISTORY, null, null, null, null,
						null, sortMethod.sqlSort());
			}
			else{
				String query = "SELECT * FROM " + DBHelper.TABLE_STOP_HISTORY + " HT INNER JOIN " + DBHelper.TABLE_FAVORITES 
						+ " F ON HT." + DBHelper.COL_STOPID + "=F." + DBHelper.COL_STOPID + " ORDER BY " + sortMethod.sqlSort()
						+ ";";
				cursor = database.rawQuery(query, null);
			}
			return cursor;
		}
}
