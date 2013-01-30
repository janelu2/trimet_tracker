package com.beagleapps.android.trimettrackerfree;

import java.util.ArrayList;

import com.beagleapps.android.trimettracker.objects.Route;
import com.google.android.maps.OverlayItem;


public class PopupOverlayItem {
	private static OverlayItem mOverlayItem;
	private static ArrayList<Route> mRouteList;
	private static String mStopID;

	public PopupOverlayItem(OverlayItem overlayItem, ArrayList<Route> routeList, String stopID) {
		setOverlayItem(overlayItem);
		setRouteList(routeList);
		setStopID(stopID);
	}
	
	public PopupOverlayItem(){
		
	}

	public static OverlayItem getOverlayItem() {
		return mOverlayItem;
	}

	public static void setOverlayItem(OverlayItem mOverlayItem) {
		PopupOverlayItem.mOverlayItem = mOverlayItem;
	}

	public static ArrayList<Route> getRouteList() {
		return mRouteList;
	}

	public static void setRouteList(ArrayList<Route> mRouteList) {
		PopupOverlayItem.mRouteList = mRouteList;
	}

	public static String getStopID() {
		return mStopID;
	}

	public static void setStopID(String mStopID) {
		PopupOverlayItem.mStopID = mStopID;
	}
}
