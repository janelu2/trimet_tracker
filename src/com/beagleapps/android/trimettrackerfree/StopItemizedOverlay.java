package com.beagleapps.android.trimettrackerfree;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.beagleapps.android.trimettracker.objects.Route;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class StopItemizedOverlay extends ItemizedOverlay {

	private ArrayList<ArrayList<Route>> mRouteList;
	private ArrayList<String> mStopIDList;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public StopItemizedOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		mRouteList = new ArrayList<ArrayList<Route>>();
		mStopIDList = new ArrayList<String>();
	}

	public void addOverlay(OverlayItem overlay, ArrayList<Route> routes, String stopID) {
		mOverlays.add(overlay);
		mRouteList.add(routes);
		mStopIDList.add(stopID);
		populate();
	}
	
	public void clearOverlays() {
		mOverlays.clear();
		mRouteList.clear();
		mStopIDList.clear();
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		
		PopupOverlayItem popupItem = new PopupOverlayItem(item, mRouteList.get(index), mStopIDList.get(index));
		
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(mContext, FindNearbyPopup.class);
		mContext.startActivity(showStopIntent);
		
		return true;
	}

}
