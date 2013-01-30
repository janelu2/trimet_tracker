package com.beagleapps.android.trimettrackerfree;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class LocationHandler {
    Timer mTimer;
    LocationManager mLocationManager;
    LocationResult locationResult;
    boolean mGPSEnabled=false;
    boolean mNetworkEnabled=false;

    public boolean getLocation(Context context, LocationResult result)
    {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult=result;
        if(mLocationManager==null)
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        //exceptions will be thrown if provider is not permitted.
        try{mGPSEnabled=mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}catch(Exception ex){}
        //try{network_enabled=lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);}catch(Exception ex){}

        //don't start listeners if no provider is enabled
        if(!mGPSEnabled && !mNetworkEnabled)
            return false;

        if(mGPSEnabled)
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        if(mNetworkEnabled)
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        mTimer=new Timer();
        mTimer.schedule(new LocationTimout(context), 15000);
        return true;
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            locationResult.gotLocation(location);
            mLocationManager.removeUpdates(this); 
            mLocationManager.removeUpdates(locationListenerNetwork);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            mTimer.cancel();
            locationResult.gotLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(locationListenerGps);
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
             mLocationManager.removeUpdates(locationListenerGps);
             mLocationManager.removeUpdates(locationListenerNetwork);

             Location net_loc=null, gps_loc=null;
             if(mGPSEnabled)
                 gps_loc=mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
             if(mNetworkEnabled)
                 net_loc=mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

             //if there are both values use the latest one
             if(gps_loc!=null && net_loc!=null){
                 if(gps_loc.getTime()>net_loc.getTime())
                     locationResult.gotLocation(gps_loc);
                 else
                     locationResult.gotLocation(net_loc);
                 return;
             }

             if(gps_loc!=null){
                 locationResult.gotLocation(gps_loc);
                 return;
             }
             if(net_loc!=null){
                 locationResult.gotLocation(net_loc);
                 return;
             }
             locationResult.gotLocation(null);
        }
    }
    
    class LocationTimout extends TimerTask {
    	Context mContext;
    	
        public LocationTimout(Context context) {
			mContext = context;
		}

		@Override
        public void run() {
             mLocationManager.removeUpdates(locationListenerGps);
             mLocationManager.removeUpdates(locationListenerNetwork);
        }
    }
    
    public void stopLocationUpdates(){
    	if(mTimer != null){
    		mTimer.cancel();
    	}
    	mLocationManager.removeUpdates(locationListenerGps);
        mLocationManager.removeUpdates(locationListenerNetwork);
    }

    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);

		public void requestTimedOut(Context context){
			
		}
    }
}