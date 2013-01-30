package com.beagleapps.android.trimettrackerfree;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;
import com.beagleapps.android.trimettracker.objects.Route;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class FindNearby extends MapActivity {
	private MapView vMapView;
	private RelativeLayout vRefreshButton;
	
	private LocationHandler.LocationResult mLocationResult;
	private Location mCurrentLocation;
	private StopItemizedOverlay mStopOverlay;
	private GPSMarkerItemizedOverylay mGPSOverlay;
	private DownloadNearbyStopsDataTask mDownloadNearbyStopsTask;
	private ProgressDialog mFindingStopsDialog;
	private ProgressDialog mGettingGPSDialog;

	private NearbyStopsDocument mStopsDocument;
	private LocationHandler mLocationHandler;
	private TextView vRouteFilterTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.findnearby);
		
		mStopsDocument = new NearbyStopsDocument();
		loadStopXML();

		vRouteFilterTextView = (TextView)findViewById(R.id.FNB_FilterTextBox);
		vMapView = (MapView) findViewById(R.id.FNBMapView);
		vRefreshButton = (RelativeLayout) findViewById(R.id.FN_refreshButton);
		vMapView.setBuiltInZoomControls(true);

		mCurrentLocation = null;

		setupMapOverylays();
		setupListeners();

		getGPSLocation();
		
		
		// TODO I'm disabling rotation for now, might fix later
		//handleRotation();

	}
	
	private void loadStopXML() {
		NearbyStopsDocument.setXMLDoc(XMLIOHelper.loadFile(getBaseContext(), NearbyStopsDocument.mFileName));
	}
	
	private void deleteStopXML() {
		// TODO Auto-generated method stub
		
	}
	
	private void onRefreshClick() {
		getGPSLocation();
	}

	/*private void handleRotation() {
		mDownloadNearbyStopsTask = (DownloadNearbyStopsDataTask)getLastNonConfigurationInstance();
		
		if (mDownloadNearbyStopsTask != null){
			mDownloadNearbyStopsTask.attach(this);
			if (mDownloadNearbyStopsTask.isDone()){
				dismissFindingStopsDialog();
			}
			else{
				showFindingStopsDialog();
			}
		}
	}*/
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadNearbyStopsTask != null){
			mDownloadNearbyStopsTask.detach();
		}

		return(mDownloadNearbyStopsTask);
	}
	
	private void setupListeners() {
		
		vRefreshButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onRefreshClick();
			}
		});
		
		vRouteFilterTextView.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {	}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {	}
			
			public void afterTextChanged(Editable s) {
				onFilterTextChange();
			}
		});
		
	}

	protected void onFilterTextChange() {
		String routeFilter = vRouteFilterTextView.getText().toString().toLowerCase();
		setupStopsOverylay(routeFilter);
	}

	private void setupMapOverylays() {
		Drawable flagMarker = this.getResources().getDrawable(R.drawable.overlay_bus_icon);
		Drawable GPSMarker = this.getResources().getDrawable(R.drawable.overlay_crosshair);
		
		mStopOverlay = new StopItemizedOverlay(flagMarker, this);
		mGPSOverlay = new GPSMarkerItemizedOverylay(GPSMarker, this);
	}

	// Takes lat/lon as double, converts to GeoPoint
	private GeoPoint getGeoPoint(double lat, double lon) {
		return new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000));
	}

	// Takes location, converts to GeoPoint
	private GeoPoint getGeoPoint(Location location) {
		// multiply by 1E6, convert to int to match what GeoPoint expects
		return new GeoPoint((int) (location.getLatitude()* 1000000), (int) (location.getLongitude() * 1000000));
	}

	private void getGPSLocation() {
		mLocationResult = new LocationHandler.LocationResult() {
			@Override
			public void gotLocation(Location location) {
				if(location != null){
					mCurrentLocation = new Location(location);
					if(mCurrentLocation != null){
						centerMapOnLocation(mCurrentLocation);
						
						// Clear out the overlays first
						vMapView.getOverlays().clear();
						placeGPSMarker(mCurrentLocation);
						dismissGettingGPSDialog();
						getTrimetData(mCurrentLocation);
					}
				}
				else{
					showError(getString(R.string.problemGettingGPS));
				}
				
			}
		};

		mLocationHandler = new LocationHandler();
		boolean success = mLocationHandler.getLocation(this, mLocationResult);
		if(success){
			showGettingGPSDialog();
		}
		else
		{
			showError(getString(R.string.GPSDisabled));
		}

	}

	protected void placeGPSMarker(Location location) {
		OverlayItem currentPosition = new OverlayItem(getGeoPoint(location), null, null);
		
		// Add overlay item
		mGPSOverlay.clearOverlays();
		mGPSOverlay.addOverlay(currentPosition);
		
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mGPSOverlay);
		// Refresh overlays
		vMapView.postInvalidate();

	}

	protected void getTrimetData(Location location) {
		String urlString = new String(getString(R.string.baseStopsSearchUrl)+ location.getLongitude() + "," + location.getLatitude());
		
		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadNearbyStopsTask = new DownloadNearbyStopsDataTask(this);
			mDownloadNearbyStopsTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
	}

	private void centerMapOnLocation(Location location) {
		MapController mapController = vMapView.getController();
		
		mapController.setZoom(16);
		mapController.animateTo(getGeoPoint(location));
	}
	
	
	// Must be called after stops have been retrieved from trimet and gps has returned a location
	private void centerMapOnStops() {
		MapController mapController = vMapView.getController();
		
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;
		
		int length = mStopsDocument.lengthLocations();
		for(int i = 0; i < length; i++){
			GeoPoint geoPoint = getGeoPoint(mStopsDocument.getLat(i), mStopsDocument.getLon(i));
			
			int lat = geoPoint.getLatitudeE6();
			int lon = geoPoint.getLongitudeE6();
			
			maxLat = Math.max(lat, maxLat);
			minLat = Math.min(lat, minLat);
			maxLon = Math.max(lon, maxLon);
			minLon = Math.min(lon, minLon);
		 }

		double fitFactor = 1.0;
		mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor), (int)(Math.abs(maxLon - minLon) * fitFactor));
		//mapController.setZoom(16);
		mapController.animateTo(getGeoPoint(mCurrentLocation));
	}

	@Override
	protected boolean isRouteDisplayed() {
		// Method required for this activity.
		return false;
	}
	
	public void setupStopsOverylay() {
		setupStopsOverylay(null);
		
	}
	
	@SuppressWarnings("unchecked")
	public void setupStopsOverylay(String routeFilter) {
		int length = mStopsDocument.lengthLocations();
		ArrayList<Route> routeList = new ArrayList<Route>();
		ArrayList<String> stopIDList = new ArrayList<String>();
		
		mStopOverlay.clearOverlays();
		
		for(int i = 0; i < length; i++){
			routeList.clear();
			GeoPoint geoPoint = getGeoPoint(mStopsDocument.getLat(i), mStopsDocument.getLon(i));
			String title = mStopsDocument.getDescription(i);
			String direction = mStopsDocument.getDirection(i);
			String stopID = mStopsDocument.getLocationID(i);
		
			for(int j = 0; j < mStopsDocument.lengthRoutes(i); j++){
				String routeNumber = mStopsDocument.getRouteNumber(i, j);
				String routeDesc = mStopsDocument.getRouteDesc(i, j);
				
				// Retrieves the name of the route if applicable (e.g. for Max lines), compares to the route filter
				if(routeFilter == null || RoutesHelper.getRouteName(routeNumber).toLowerCase().contains(routeFilter.toLowerCase())){
					routeList.add(new Route(routeDesc, routeNumber));
				}
			}
			
			if(routeList.size() > 0){
				OverlayItem item = new OverlayItem(geoPoint, title, direction);
				
				// Add overlay item
				mStopOverlay.addOverlay(item, (ArrayList<Route>) routeList.clone(), stopID);
			}
		}
		
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mStopOverlay);
		vMapView.postInvalidate();
	}

	protected void showError(Context context, String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT)
				.show();
	}
	
	protected void showError(String error) {
		showError(getApplicationContext(), error);
	}
	
	private void showGettingGPSDialog(){
		setupGettingGPSDialog();
		mGettingGPSDialog.show();
	}

	private void dismissGettingGPSDialog(){
		mGettingGPSDialog.dismiss();
	}
	
	private void setupGettingGPSDialog(){
		mGettingGPSDialog = null;
		mGettingGPSDialog = new ProgressDialog(this);
		mGettingGPSDialog.setMessage(getString(R.string.dialogFindingLocation));
		mGettingGPSDialog.setIndeterminate(true);
		mGettingGPSDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				mLocationHandler.stopLocationUpdates();
			}
		};
		
		mGettingGPSDialog.setOnCancelListener(onCancelListener);
	}
	
	private void showFindingStopsDialog(){
		setupFindingStopsDialog();
		mFindingStopsDialog.show();
	}

	private void dismissFindingStopsDialog(){
		mFindingStopsDialog.dismiss();
	}
	
	private void setupFindingStopsDialog(){
		mFindingStopsDialog = null;
		mFindingStopsDialog = new ProgressDialog(this);
		mFindingStopsDialog.setMessage(getString(R.string.dialogFindingStops));
		mFindingStopsDialog.setIndeterminate(true);
		mFindingStopsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				mDownloadNearbyStopsTask.cancel(true);
			}
		};
		
		mFindingStopsDialog.setOnCancelListener(onCancelListener);
	}
	
	class DownloadNearbyStopsDataTask extends DownloadXMLAsyncTask<FindNearby> {
		private String mFileName = "DownloadNearbyStopsData.xml";
		
		public DownloadNearbyStopsDataTask(FindNearby activity) {
			super(activity, getApplicationContext());
		}

		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				activity.showFindingStopsDialog();
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissFindingStopsDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					NearbyStopsDocument.setXMLDoc(newXmlHandler.getXmlDoc());
				
					activity.setupStopsOverylay();
					activity.centerMapOnStops();
				}
			}
			else{
				Log.w(TAG, "FindNearby activity is null");
			}
		}

		@Override
		protected String getFileName() {
			return mFileName;
		}
	}
}
