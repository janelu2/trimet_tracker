package com.beagleapps.android.trimettrackerfree;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.adapters.DBAdapter;
import com.beagleapps.android.trimettracker.adapters.HistoryEntryAdapter;
import com.beagleapps.android.trimettracker.helpers.FavoritesHelper;
import com.beagleapps.android.trimettracker.helpers.HistoryHelper;
import com.beagleapps.android.trimettracker.helpers.PreferencesHelper;
import com.beagleapps.android.trimettracker.helpers.ThemeHelper;
import com.beagleapps.android.trimettracker.helpers.PreferencesHelper.Preferences;
import com.beagleapps.android.trimettracker.objects.HistoryEntry;

public class MainView extends Activity {
	/** Called when the activity is first created. */

	@SuppressWarnings("unused")
	private static String TAG = "homepage";

	private ArrayList<HistoryEntry> mStopsList = null;
	private HistoryEntryAdapter mFavoriteAdapter;
	private DBAdapter mDbHelper;

	private ListView vFavoriteStopsListView;
	private View vEmptyView;
	
	//private PopupWindow vWelcomePopup;

	private TextView vStopIDTextBox;
	private Button vGoButton;
	private TextView vSortText;
	private TextView vDisplayChoiceText;
	private RelativeLayout vSortButton;
	private RelativeLayout vDisplayChoiceButton;

	private static ArrivalsDocument mArrivalsDoc;
	ArrayAdapter<String> mListViewAdapter;
	
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadRoutesDataTask mDownloadRoutesTask = null;
	private ProgressDialog mArrivalsDialog;
	private ProgressDialog mRoutesDialog;

	private AlertDialog mChooseSortAlert;

	private AlertDialog mChooseDisplayChoiceAlert;

	private AlertDialog mLongPressAlert;

	private int mLongPressValue;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		mDbHelper = new DBAdapter(this);
		mDbHelper.open();
		
		ThemeHelper.setThemeOnCreate(this, mDbHelper);
		setContentView(R.layout.main);

		vFavoriteStopsListView = (ListView)findViewById(R.id.favoriteStopsListView);
		vEmptyView = (View)findViewById(R.id.HP_emptyView);
		
		setupButtons();
		
		vStopIDTextBox = (TextView)findViewById(R.id.stopIDTextBox);

		mStopsList = new ArrayList<HistoryEntry>();

		getStopsFromDatabase();

		mFavoriteAdapter = new HistoryEntryAdapter(this, mStopsList);
		vFavoriteStopsListView.setAdapter(mFavoriteAdapter);
		
		vFavoriteStopsListView.setEmptyView(vEmptyView);

		setupListeners();
		
		handleRotation();

		checkVersion();
	}


	public void setupButtons() {
		vGoButton = (Button)findViewById(R.id.goButton);
		vSortText = (TextView)findViewById(R.id.MV_sortChoice);
		vDisplayChoiceText = (TextView)findViewById(R.id.MV_displayChoice);
		vDisplayChoiceButton = (RelativeLayout)findViewById(R.id.MV_displayChoiceButton);
		vSortButton = (RelativeLayout)findViewById(R.id.MV_sortChoiceButton);

		refreshSortButton();
		refreshDisplayChoiceButton();
	}


	public void refreshSortButton() {
		switch(PreferencesHelper.getSort(mDbHelper.getDatabase())){
		case VISITS:
			vSortText.setText(getText(R.string.MV_sortMostVisited));
			break;
		case TIME:
			vSortText.setText(getText(R.string.MV_sortMostRecent));
			break;
		case STOPID:
			vSortText.setText(getText(R.string.MV_sortStopID));
			break;
		default:
			break;
		}
	}
	
	public void refreshDisplayChoiceButton() {
		switch(PreferencesHelper.getDisplayChoice(mDbHelper.getDatabase())){
		case ALL:
			vDisplayChoiceText.setText(getText(R.string.MV_allStops));
			break;
		case FAVORITES:
			vDisplayChoiceText.setText(getText(R.string.MV_favorites));
			break;
		default:
			break;
		}
	}


	private void checkVersion() {
		try {
			String currentVersion = ManifestHelper.getCurrentVersion(this);
			String dbVersion = mDbHelper.fetchVersion();
			//showError("Manifest Version: "+currentVersion+ " DB Version: "+ dbVersion);
			if (dbVersion.compareTo(currentVersion) != 0 || dbVersion.equals(DBAdapter.NO_VERSION_FOUND)){
				mDbHelper.setCurrentVersion(currentVersion);
				showNewFeaturesDialog();
			}
		} catch (Exception e) {
			showError(e.toString());
		}
		
	}

	private void setupListeners() {
		vGoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (vStopIDTextBox.getText().length() > 0){
					onStopClick(Integer.parseInt(vStopIDTextBox.getText().toString()));
				}
				else{
					showError(getString(R.string.errorNoStopID));
				}
			}
		});
		
		vSortButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showSortDialog();
			}
		});
		
		vDisplayChoiceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showDisplayChoiceDialog();
			}
		});

		vFavoriteStopsListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onStopClick(mStopsList.get(position).getStopID());
			}
		});
		
		vFavoriteStopsListView.setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				showLongPressDialog(position);
				return true;
			}
		});
	}

	protected void showDisplayChoiceDialog() {
		final CharSequence[] items = {getText(R.string.MV_allStops), getText(R.string.MV_favorites)};
		
		int currentDisplayChoice = PreferencesHelper.getPreference(mDbHelper.getDatabase(), Preferences.DISPLAY_CHOICE).value;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.chooseDisplayChoice));
		builder.setSingleChoiceItems(items, currentDisplayChoice, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int newDisplayChoice) {
		        PreferencesHelper.setDisplayChoice(mDbHelper.getDatabase(), newDisplayChoice);
		        refreshDisplayChoiceButton();
		        mChooseDisplayChoiceAlert.dismiss();
		    }
		});
		mChooseDisplayChoiceAlert = builder.create();
		
		mChooseDisplayChoiceAlert.show();
	}


	protected void showSortDialog() {
		final CharSequence[] items = {
			getText(R.string.MV_sortMostVisited),
			getText(R.string.MV_sortMostRecent),
			getText(R.string.MV_sortStopID)
		};
		
		int currentSort = PreferencesHelper.getPreference(mDbHelper.getDatabase(), Preferences.SORT).value;

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.chooseSortMethod));
		builder.setSingleChoiceItems(items, currentSort, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int newSort) {
		        PreferencesHelper.setSort(mDbHelper.getDatabase(), newSort);
		        refreshSortButton();
		        mChooseSortAlert.dismiss();
		    }
		});
		mChooseSortAlert = builder.create();
		
		mChooseSortAlert.show();
	}
	
	protected void showLongPressDialog(int position) {
		mLongPressValue = position;
		CharSequence[] items = null;
		
		// If it's a favorite, setup a different list
		if(FavoritesHelper.checkForFavorite(mDbHelper.getDatabase(), mStopsList.get(mLongPressValue).getStopID())){
			items = new CharSequence[]{
					getText(R.string.MV_deleteFromHistory),
					getText(R.string.MV_deleteFromFavorites),
					getText(R.string.MV_deleteFromBoth)
			};
		}
		else{
			items = new CharSequence[]{getText(R.string.MV_deleteFromHistory)};
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getText(R.string.chooseOption));
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int position) {
		    	int stopID = mStopsList.get(mLongPressValue).getStopID();
		    	
		    	switch(position){
		    	case 0:
		    		// Delete from history
		    		HistoryHelper.deleteHistoryEntry(mDbHelper.getDatabase(), stopID);
		    		break;
		    	case 1:
		    		// Delete from favs
		    		FavoritesHelper.deleteFavorite(mDbHelper.getDatabase(), stopID);
		    		break;
		    	case 2:
		    		// Delete from both
		    		HistoryHelper.deleteHistoryEntry(mDbHelper.getDatabase(), stopID);
		    		FavoritesHelper.deleteFavorite(mDbHelper.getDatabase(), stopID);
		    		break;
		    	}
		        
		    	mLongPressAlert.dismiss();
		    }
		});
		mLongPressAlert = builder.create();
		
		mLongPressAlert.show();
	}


	// Resumes the dialog if an async task is still in progress after rotation
	private void handleRotation() {
		HomepageRotationInstance instance = (HomepageRotationInstance)getLastNonConfigurationInstance();
		
		if (instance != null){
			mDownloadArrivalTask = instance.getDownloadArrivalTask();
			mDownloadRoutesTask = instance.getDownloadRoutesTask();
		}
		
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.attach(this, getApplicationContext());
			if (mDownloadArrivalTask.isDone()){
				dismissArrivalsDialog();
			}
			else{
				showArrivalsDialog();
			}
		}
		else if (mDownloadRoutesTask != null){
			mDownloadRoutesTask.attach(this, getApplicationContext());
			if (mDownloadRoutesTask.isDone()){
				dismissRoutesDialog();
			}
			else{
				showRoutesDialog();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.homepage_menu, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_findStop) {
			onFindStopClick();
			return true;
		} else if (item.getItemId() == R.id.menu_nearbyStops) {
			onFindNearybyClick();
			return true;
		} else if (item.getItemId() == R.id.menu_about) {
			onAboutClick();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void onAboutClick() {
		Intent aboutIntent = new Intent();
		aboutIntent.setClass(getApplicationContext(), AboutScreen.class);
		startActivity(aboutIntent);
	}

	private void onFindNearybyClick() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), FindNearby.class);
		startActivity(showStopIntent);
	}

	private void showNewFeaturesDialog() {
		try {
			final Dialog dialog = new Dialog(MainView.this);
			String title = getString(R.string.app_name) + " " +ManifestHelper.getCurrentVersion(this);
			
	        dialog.setContentView(R.layout.welcome_popup);
	        dialog.setTitle(title);
	        dialog.setCancelable(true);

	        //set up text
	        TextView text = (TextView) dialog.findViewById(R.id.PopupMessage);
	        text.setText(R.string.PopupMessage);

	        //set up button
	        Button button = (Button) dialog.findViewById(R.id.PopupButton);
	        button.setOnClickListener(new OnClickListener() {
		        public void onClick(View v) {
		                dialog.dismiss();
	            }
	        });
	       
	        LayoutParams params = dialog.getWindow().getAttributes(); 
            params.width = LayoutParams.FILL_PARENT; 
            dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	        dialog.show();
		} catch (Exception e) {
			showError(e.toString());
		}
	}
	

	private void onFindStopClick() {
		String urlString = new String(getString(R.string.baseRoutesURL));
		
		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadRoutesTask = new DownloadRoutesDataTask(this);
			mDownloadRoutesTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
		
	}

	@Override 
	protected void onPause(){
		super.onPause();
		//mDbHelper.close();
	}
	
	@Override 
	protected void onDestroy(){
		super.onDestroy();
		mDbHelper.close();
	}

	public void onWindowFocusChanged (boolean hasFocus){
		if(hasFocus){
			getStopsFromDatabase();
			mFavoriteAdapter.notifyDataSetChanged();
		}
	}

	protected void onStopClick(int stopID) {
		String urlString = new String(getString(R.string.baseArrivalURL)+ stopID);


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadArrivalTask = new DownloadArrivalDataTask(this);
			mDownloadArrivalTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}

	}

	protected void launchShowStop() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), ShowStop.class);
		startActivity(showStopIntent);
	}
	
	public void launchChooseRoute() {
		Intent chooseRouteIntent = new Intent();
		chooseRouteIntent.setClass(getApplicationContext(), ChooseRoute.class);
		startActivity(chooseRouteIntent);
	}


	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}


	private void getStopsFromDatabase() {
		Cursor cursor = HistoryHelper.fetchStopsUsingPrefs(mDbHelper.getDatabase());
		cursor.moveToFirst();

		mStopsList.clear();
		while (!cursor.isAfterLast()){
			HistoryEntry stop = HistoryHelper.constructHistoryFromCursor(cursor); 
			mStopsList.add(stop);
			cursor.moveToNext();
		}
		
		cursor.close();
	}


	public void setXmlArrivalsDoc(ArrivalsDocument xmlArrivalsDoc) {
		MainView.mArrivalsDoc = xmlArrivalsDoc;
	}


	public ArrivalsDocument getXmlArrivalsDoc() {
		return mArrivalsDoc;
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
		}
		if (mDownloadRoutesTask != null){
			mDownloadRoutesTask.detach();
		}
		
		return(new HomepageRotationInstance(mDownloadArrivalTask, mDownloadRoutesTask));
	}
	
	private void setupArrivalsDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				MainView.this.mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}
	
	private void setupRoutesDialog(){
		mRoutesDialog = null;
		mRoutesDialog = new ProgressDialog(this);
		mRoutesDialog.setMessage(getString(R.string.dialogGettingRoutes));
		mRoutesDialog.setIndeterminate(true);
		mRoutesDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				MainView.this.mDownloadRoutesTask.cancel(true);
			}
		};
		
		mRoutesDialog.setOnCancelListener(onCancelListener);
	}

	void showArrivalsDialog(){
		setupArrivalsDialog();
		mArrivalsDialog.show();
	}

	void dismissArrivalsDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
		}
	}
	
	void showRoutesDialog(){
		setupRoutesDialog();
		mRoutesDialog.show();
	}

	void dismissRoutesDialog(){
		if (mRoutesDialog != null){
			mRoutesDialog.dismiss();
		}
	}
	
	class DownloadRoutesDataTask extends DownloadXMLAsyncTask<MainView> {
		
		DownloadRoutesDataTask(MainView activity) {
			super(activity, getApplicationContext());
		}


		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				activity.showRoutesDialog();
			}
			else{
				Log.w(TAG, "homepage activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissRoutesDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					RoutesDocument.mRouteXMLDoc = newXmlHandler.getXmlDoc();
					
					activity.launchChooseRoute();
				}
			}
			else{
				Log.w(TAG, "homepage activity is null");
			}
		}

		@Override
		protected String getFileName() {
			return RoutesDocument.mRoutesFileName;
		}
	}
	
	class DownloadArrivalDataTask extends DownloadXMLAsyncTask<MainView> {
		
		DownloadArrivalDataTask(MainView activity) {
			super(activity, getApplicationContext());
		}

		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				activity.showArrivalsDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissArrivalsDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					ArrivalsDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					ArrivalsDocument.mRequestTime = newXmlHandler.getRequestTime();
					
					activity.launchShowStop();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		@Override
		protected String getFileName() {
			return ArrivalsDocument.mFileName;
		}
	}
}