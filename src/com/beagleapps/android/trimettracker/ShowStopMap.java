package com.beagleapps.android.trimettracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettrackerfree.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class ShowStopMap extends MapActivity {
	private MapView vMapView;
	private TextView vTitle;
	private RelativeLayout vGpsButton;
	private LocationHandler.LocationResult mLocationResult;
	private LocationHandler mLocationHandler;
	private Location mCurrentLocation;
	
	private ShowMapItemizedOverlay mStopOverlay;
	private GPSMarkerItemizedOverylay mGPSOverlay;
	
	private ProgressDialog mGettingGPSDialog;
	private String mStopDescription;
	private float mLatitude;
	private float mLongitude;
	private int mStopID;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.show_on_map);
		
		getIntentData();
		
		vMapView = (MapView) findViewById(R.id.SOM_MapView);
		vTitle = (TextView) findViewById(R.id.SOM_Title);
		vGpsButton = (RelativeLayout) findViewById(R.id.SOM_gpsButton);
		vMapView.setBuiltInZoomControls(true);
		
		vTitle.setText(mStopID + ": " + mStopDescription);

		mCurrentLocation = null;

		setupMapOverylays();
		
		setupListeners();
		
		centerMapOnStop();
	}
	
	private void setupListeners() {
		vGpsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				getGPSLocation();
			}
		});
	}
	
	private void centerMapOnStop() {
		centerMapOnLocation(mLatitude, mLongitude);
		
	}

	private void getIntentData() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
		    mStopDescription = extras.getString("stopDescription");
		    mStopID = extras.getInt("stopID");
		    mLatitude = extras.getFloat("latitude");
		    mLongitude = extras.getFloat("longitude");
		}
		else{
			mStopDescription = "Error";
		    mLatitude = 0;
		    mLongitude = 0;
		    mStopID = 0;
			
		}
	}
	
	private void onGPSClick() {
		getGPSLocation();
	}

	private void setupMapOverylays() {
		Drawable flagMarker = this.getResources().getDrawable(R.drawable.overlay_bus_icon);
		Drawable GPSMarker = this.getResources().getDrawable(R.drawable.overlay_crosshair);
		
		mStopOverlay = new ShowMapItemizedOverlay(flagMarker, this);
		mGPSOverlay = new GPSMarkerItemizedOverylay(GPSMarker, this);
	
		GeoPoint geoPoint = getGeoPoint(mLatitude, mLongitude);
		OverlayItem item = new OverlayItem(geoPoint, null, null);
		mStopOverlay.addOverlay(item);
		vMapView.getOverlays().add(mStopOverlay);
		vMapView.postInvalidate();
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
						placeGPSMarker(mCurrentLocation);
						centerMapOnMidpoint(getGeoPoint(mCurrentLocation), getGeoPoint(mLatitude, mLongitude));
						dismissGettingGPSDialog();
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
		
		// Remove gps overlay first
		vMapView.getOverlays().remove(mGPSOverlay);
		
		// Add overlay item to overlay
		mGPSOverlay.clearOverlays();
		mGPSOverlay.addOverlay(currentPosition);
		
		// Then add the overlay list to the mapview
		vMapView.getOverlays().add(mGPSOverlay);
		// Refresh overlays
		vMapView.postInvalidate();

	}

	private void centerMapOnMidpoint(GeoPoint first, GeoPoint second) {
		MapController mapController = vMapView.getController();

		int minLat = (int) Math.min(first.getLatitudeE6(), second.getLatitudeE6());
		int maxLat = (int) Math.max(first.getLatitudeE6(), second.getLatitudeE6());
		int minLon = (int) Math.min(first.getLongitudeE6(), second.getLongitudeE6());
		int maxLon = (int) Math.max(first.getLongitudeE6(), second.getLongitudeE6());
		
		double fitFactor = 1.1;
		mapController.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor), (int)(Math.abs(maxLon - minLon) * fitFactor));
		mapController.animateTo(new GeoPoint( (maxLat + minLat)/2, (maxLon + minLon)/2 )); 
	}
	
	private void centerMapOnLocation(Location location) {
		MapController mapController = vMapView.getController();
		GeoPoint currentGeoPoint = getGeoPoint(location);
		mapController.animateTo(currentGeoPoint);
		mapController.setZoom(16);
	}
	
	private void centerMapOnLocation(float lat, float lon) {
		MapController mapController = vMapView.getController();

		GeoPoint currentGeoPoint = getGeoPoint(lat, lon);
		mapController.animateTo(currentGeoPoint);
		mapController.setZoom(16);
	}

	
	public void setupStopsOverylay() {
		setupStopsOverylay(null);
		
	}
	
	@SuppressWarnings("unchecked")
	public void setupStopsOverylay(String routeFilter) {
		
		
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

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
}
