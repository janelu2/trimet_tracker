package com.beagleapps.android.trimettrackerfree;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Document;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.adapters.ArrivalAdapter;
import com.beagleapps.android.trimettracker.adapters.DBAdapter;
import com.beagleapps.android.trimettracker.helpers.FavoritesHelper;
import com.beagleapps.android.trimettracker.helpers.HistoryHelper;
import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;
import com.beagleapps.android.trimettracker.objects.Arrival;
import com.beagleapps.android.trimettracker.objects.Favorite;
import com.beagleapps.android.trimettracker.objects.HistoryEntry;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class ShowStop extends Activity {

	private final int COUNTDOWN_DELAY = 5000;
	private final int REFRESH_DELAY = 30000;
	private final int MAX_AGE = 10;

	String TAG = "ShowStop";
	private int mStopID;
	private int mLastVisited;
	private int mVisits;

	private ArrayList<Arrival> mArrivals = null;
	private ArrivalAdapter mArrivalAdapter;

	private TextView vStopTitle;
	private TextView vDirection;
	private ListView vArrivalsListView;
	private View vEmptyView;
	private ArrivalsDocument mArrivalsDoc;

	private DBAdapter mDbHelper;
	private boolean mIsFavorite;

	private Handler mTimersHandler = new Handler();
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadDetourDataTask mDownloadDetourTask = null;
	private ProgressDialog mArrivalsDialog;
	private ProgressDialog mDetoursDialog;
	private Button vDetourButton;
	private View vBottomDivider;
	private LinearLayout vBottomBar;
	
	// Set when dialog is canceled
	private long mRefreshDelayTime;
	
	// Admob Ad
	private AdView vAdView;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.showstop);

		mDbHelper = new DBAdapter(this);
		mDbHelper.open();

		mArrivals = new ArrayList<Arrival>();
		mArrivalsDoc = new ArrivalsDocument();
		loadStopXML();
		
		if(ArrivalsDocument.isNull()){
			// Something has gone terribly wrong
			finish();
		}
		
		mStopID = mArrivalsDoc.getStopID();
		setupViews();
		
		
		
		getArrivals();

		mArrivalAdapter = new ArrivalAdapter(this, mArrivals);
		vArrivalsListView.setAdapter(mArrivalAdapter);
		vArrivalsListView.setEmptyView(vEmptyView);

		handleRotation();
		setupListeners();
		
		// Check for detours, display button
		handleDetours();
		
		// Add stop to history
		updateHistory();
		
		// Uncomment for the free version
		setupAd();
	}

	public void setupViews() {
		vArrivalsListView = (ListView)findViewById(R.id.SS_ArrivalsListView);
		vEmptyView = (View)findViewById(R.id.SS_emptyView);
		vStopTitle = (TextView)findViewById(R.id.SS_StopTitle);
		vDirection= (TextView)findViewById(R.id.SS_StopID);
		vDetourButton = (Button)findViewById(R.id.SS_DetourButton);
		vBottomBar = (LinearLayout)findViewById(R.id.SS_BottomBar);
		vBottomDivider = (View)findViewById(R.id.SS_BottomDivider);
		
		vDirection.setText(mArrivalsDoc.getDirection());
		vStopTitle.setText(mStopID + ": " + mArrivalsDoc.getStopDescription());
	}
	
	private void updateHistory() {
		HistoryEntry entry = HistoryHelper.getHistoryEntry(mDbHelper.getDatabase(), mStopID);
		
		// if entry already exists, check to see if it's old
		if(entry != null){
			mLastVisited = entry.getLastVisited();
			mVisits = entry.getVisits();
			
			// if old, update the history, else do nothing
			if(HistoryHelper.isOutOfDate(mLastVisited)){
				mVisits ++;
				mLastVisited = HistoryHelper.getCurrentTime();
				
				entry.setLastVisited(mLastVisited);
				entry.setVisits(mVisits);
				
				HistoryHelper.updateHistory(mDbHelper.getDatabase(), entry);
			}
		}
		// else make a new entry and insert it
		else{
			mLastVisited = HistoryHelper.getCurrentTime();
			mVisits = 1;
			
			entry = constructHistoryEntry();
			HistoryHelper.updateHistory(mDbHelper.getDatabase(), entry);
		}
	}

	private void setupAd() {
		// Create the adView
		vAdView = new AdView(this, AdSize.BANNER, getString(R.string.admob_publisher_id));

	    LinearLayout layout = (LinearLayout)findViewById(R.id.showstopLinearLayout);

	    // Add the adView to it
	    layout.addView(vAdView);

	    // Initiate a generic request to load it with an ad
	    AdRequest adRequest = new AdRequest();
	    //adRequest.addTestDevice("F51ECFD2E2C9524C7DA20C5C59C8DE1A");
	    vAdView.loadAd(adRequest);
		
	}

	private void handleRotation() {
		ShowStopRotationInstance instance = (ShowStopRotationInstance)getLastNonConfigurationInstance();
		
		if (instance != null){
			mDownloadArrivalTask = instance.getDownloadArrivalTask();
			mDownloadDetourTask = instance.getDownloadDetoursTask();
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
		else if (mDownloadDetourTask != null){
			mDownloadDetourTask.attach(this, getApplicationContext());
			if (mDownloadDetourTask.isDone()){
				dismissDetoursDialog();
			}
			else{
				showDetoursDialog();
			}
		}
	}
	
	private void handleDetours() {
		int length = mArrivals.size();
		boolean hasDetour = false;
	
		for (int index = 0; index < length; index++) {
			if (mArrivalsDoc.hasDetour(index)){
				hasDetour = true;
				break;
			}
		}
		
		if(hasDetour){
			showDetourButton();
		}
	}

	private void setupListeners() {
		vDetourButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onDetourClick();
			}
		});
	}
	
	private void onDetourClick() {
		String routeListString = RoutesHelper.join(mArrivalsDoc.getRouteList(), ",");
		String urlString = new String(getString(R.string.baseDetourUrl) + routeListString);


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			stopTimers();
			mDownloadDetourTask = new DownloadDetourDataTask(this);
			mDownloadDetourTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
	}
	
	@SuppressWarnings("unused")
	private void hideDetourButton() {
		vBottomBar.setVisibility(View.GONE);
		vBottomDivider.setVisibility(View.GONE);
	}
	
	private void showDetourButton() {
		vBottomBar.setVisibility(View.VISIBLE);
		vBottomDivider.setVisibility(View.VISIBLE);
	}

	// Receiver tells the app to refresh on unlock,
	// If the window has focus
	@Override 
	protected void onResume(){
		super.onResume();
		loadStopXML();
		startTimers();
	}

	private boolean isDataOutOfDate() {
		// if use cancels a dialog that's refreshing the arrival times, it will immediately refresh again
		// 		since the focus changes again and it sees that the arrivals are out of date. The refresh delay
		//		compensates for this by giving a small amount of padding
		return (mArrivalsDoc.getAge() >= MAX_AGE && getRefreshDelayAge() >= MAX_AGE);
	}

	
	private int getRefreshDelayAge() {
		return (int) ((new Date().getTime() - mRefreshDelayTime)/1000);
	}

	@Override 
	protected void onPause(){
		super.onPause();
		stopTimers();
	}
	
	
	private void loadStopXML() {
		Document loadedXML = XMLIOHelper.loadFile(getBaseContext(), ArrivalsDocument.mFileName);
		if(loadedXML != null){
			ArrivalsDocument.mXMLDoc = loadedXML;
		}
	}
	
	private void deleteStopXML() {
		// TODO Auto-generated method stub
		
	}

	@Override 
	protected void onDestroy(){
		
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
		}
		
		mDbHelper.close();
		deleteStopXML();
		
		super.onDestroy();
	}

	public void onWindowFocusChanged (boolean hasFocus){
		if (hasFocus && isDataOutOfDate()){
			loadStopXML();
			refreshArrivalData();
			resetTimers();
		}
	}

	private void startTimers() {
		mTimersHandler.postDelayed(mCountDownTask, COUNTDOWN_DELAY);
		mTimersHandler.postDelayed(mRefreshTask, REFRESH_DELAY);
	}

	private void stopTimers() {
		mTimersHandler.removeCallbacks(mRefreshTask);
		mTimersHandler.removeCallbacks(mCountDownTask);
	}

	private void resetTimers() {
		stopTimers();
		startTimers();
	}
	
	private void resetRefreshDelay() {
		mRefreshDelayTime = new Date().getTime();
	}

	private boolean isStopFavorite() {
		mIsFavorite = FavoritesHelper.checkForFavorite(mDbHelper.getDatabase(), mStopID);

		return mIsFavorite;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.showstop_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// See if the stop is favorited
		MenuItem favoriteButton = menu.findItem(R.id.menuFavorite);
		if (isStopFavorite()){
			favoriteButton.setTitle(R.string.favorited);
			favoriteButton.setIcon(R.drawable.rate_star_med_on);
		}
		else{
			favoriteButton.setTitle(R.string.addToFavorites);
			favoriteButton.setIcon(R.drawable.ic_menu_star);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
            case R.id.menuRefresh:
                    onRefreshClick();
                    return true;
            case R.id.menuFavorite:
                    onFavoriteClick();
                    return true;
            case R.id.menuSchedule:
	                onScheduleClick();
	                return true;
            case R.id.menuShowOnMap:
                    onMapClick();
                    return true;
            default:
                    return super.onOptionsItemSelected(item);
            }
    }

	private void onScheduleClick() {
		stopTimers();
		
		String url = getString(R.string.baseScheduleUrl) + mStopID;  
		Intent intent = new Intent(Intent.ACTION_VIEW);  
		intent.setData(Uri.parse(url)); 
		startActivity(intent);  
	}
	
	private void onMapClick() {
		stopTimers();
		
		Intent intent = new Intent(getApplicationContext(), ShowStopMap.class);
		intent.putExtra("stopDescription", mArrivalsDoc.getStopDescription());
		intent.putExtra("stopID", mArrivalsDoc.getStopID());
		intent.putExtra("latitude", mArrivalsDoc.getLatitude());
		intent.putExtra("longitude", mArrivalsDoc.getLongitude());
 
		startActivity(intent);  
	}

	private void onRefreshClick() {
		
		refreshArrivalData();
	}

	private void refreshArrivalData() {
		stopTimers();
		
		String urlString = new String(getString(R.string.baseArrivalURL)+ 
				mStopID);

		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadArrivalTask = new DownloadArrivalDataTask(this);
			mDownloadArrivalTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}

	}

	private void refreshStopList() {
		getArrivals();
		mArrivalAdapter.notifyDataSetChanged();
	}

	private void onFavoriteClick() {
		if (isStopFavorite()){
			// Remove stop from favorites
			FavoritesHelper.deleteFavorite(mDbHelper.getDatabase(), mStopID);
			ShowRemovedToast();
		}
		else{
			// Add stop to favorites
			FavoritesHelper.createFavorite(mDbHelper.getDatabase(), constructFavorite());
			ShowAddedToast();
		}

	}

	private void ShowAddedToast() {
		Toast.makeText(getApplicationContext(), getString(R.string.stopFavorited),
				Toast.LENGTH_SHORT).show();
	}

	private void ShowRemovedToast() {
		Toast.makeText(getApplicationContext(), getString(R.string.stopRemoved),
				Toast.LENGTH_SHORT).show();
	}

	private Favorite constructFavorite() {
		Favorite fav = new Favorite();
		if (mArrivalsDoc != null){
			fav.setDescription(mArrivalsDoc.getStopDescription());
			fav.setStopID(mArrivalsDoc.getStopID());
			fav.setDirection(mArrivalsDoc.getDirection());
			String routes = RoutesHelper.parseRouteList(mArrivalsDoc.getRouteList());
			fav.setRoutes(routes);
		}
		return fav;
	}
	
	private HistoryEntry constructHistoryEntry() {
		HistoryEntry entry = new HistoryEntry();
		if (mArrivalsDoc != null){
			entry.setDescription(mArrivalsDoc.getStopDescription());
			entry.setStopID(mArrivalsDoc.getStopID());
			entry.setDirection(mArrivalsDoc.getDirection());
			String routes = RoutesHelper.parseRouteList(mArrivalsDoc.getRouteList());
			entry.setRoutes(routes);
			
			entry.setLastVisited(mLastVisited);
			entry.setVisits(mVisits);
		}
		return entry;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadArrivalTask != null){
			mDownloadArrivalTask.detach();
		}
		if (mDownloadDetourTask != null){
			mDownloadDetourTask.detach();
		}
		
		return(new ShowStopRotationInstance(mDownloadArrivalTask, mDownloadDetourTask));
	}


	private void showDetoursDialog(){
		setupDetoursDialog();
		mDetoursDialog.show();
	}

	private void dismissDetoursDialog(){
		if (mDetoursDialog != null){
			mDetoursDialog.dismiss();
		}
	}
	
	private void setupDetoursDialog(){
		mDetoursDialog = null;
		mDetoursDialog = new ProgressDialog(this);
		mDetoursDialog.setMessage(getString(R.string.dialogGettingDetours));
		mDetoursDialog.setIndeterminate(true);
		mDetoursDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				ShowStop.this.mDownloadDetourTask.cancel(true);
			}
		};
		
		mDetoursDialog.setOnCancelListener(onCancelListener);
	}
	
	private void setupArrivalsDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				ShowStop.this.mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}

	private void showArrivalsDialog(){
		setupArrivalsDialog();
		mArrivalsDialog.show();
	}

	private void dismissArrivalsDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
		}
	}

	private void getArrivals()
	{
		mArrivals.clear();

		if(mArrivalsDoc != null){
			int length = mArrivalsDoc.getNumArrivals();

			for (int index = 0; index < length; index++){
				Arrival newArrival = new Arrival();

				newArrival.setBusDescription(mArrivalsDoc.getBusDescription(index));
				newArrival.setScheduledTime(mArrivalsDoc.getScheduledTime(index));
				newArrival.setScheduledTimeText(mArrivalsDoc.getScheduledTimeText(index));
				if (mArrivalsDoc.isEstimated(index)){
					newArrival.setArrivalTime(mArrivalsDoc.getEstimatedTime(index));
					newArrival.setEstimated(true);
				}
				else{
					newArrival.setArrivalTime("");
					newArrival.setEstimated(false);
				}
				newArrival.setRemainingMinutes(mArrivalsDoc.getRemainingMinutes(index));
				mArrivals.add(newArrival);
			}
		}
	}
	
	protected void launchShowDetour() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), ShowDetour.class);
		startActivity(showStopIntent);
	}

	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}

	class DownloadArrivalDataTask extends DownloadXMLAsyncTask<ShowStop>{
		
		DownloadArrivalDataTask(ShowStop activity) {
			super(activity, getApplicationContext());
		}

		protected void onPreExecute() {
			isDone = false;

			if (activity != null){
				activity.showArrivalsDialog();
			}
			else{
				Log.w(TAG, "ShowStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissArrivalsDialog();
				newXmlHandler.getError();
				if (newXmlHandler.hasError()){
					activity.showError(newXmlHandler.getError());
					activity.resetRefreshDelay();
				}
				else{
					ArrivalsDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					ArrivalsDocument.mRequestTime = newXmlHandler.getRequestTime();
					
					activity.refreshStopList();
				}
			}
			else{
				Log.w(TAG, "ShowStop activity is null");
			}
		}
		
		@Override
	    protected void onCancelled() {
			isDone = true;
			activity.resetRefreshDelay();
			activity.resetTimers();
	    }

		@Override
		protected String getFileName() {
			return ArrivalsDocument.mFileName;
		}

		
	}

	
	class DownloadDetourDataTask extends DownloadXMLAsyncTask<ShowStop>{
		DownloadDetourDataTask(ShowStop activity) {
			super(activity, getApplicationContext());
		}
		
		protected void onPreExecute() {
			isDone = false;

			if (activity != null){
				activity.showDetoursDialog();
			}
			else{
				Log.w(TAG, "ShowStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissDetoursDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					DetoursDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					
					activity.launchShowDetour();
				}
			}
			else{
				Log.w(TAG, "ShowStop activity is null");
			}
		}
		
		@Override
	    protected void onCancelled() {
			isDone = true;
			activity.startTimers();
			activity.resetRefreshDelay();
	    }

		@Override
		protected String getFileName() {
			return DetoursDocument.mFileName;
		}
	}


	private Runnable mCountDownTask = new Runnable() {
		public void run() {
			refreshStopList();
			mTimersHandler.postDelayed(mCountDownTask, COUNTDOWN_DELAY);
		}
	};

	private Runnable mRefreshTask = new Runnable() {
		public void run() {
			refreshArrivalData();
			mTimersHandler.postDelayed(mRefreshTask, REFRESH_DELAY);
		}
	};
}
