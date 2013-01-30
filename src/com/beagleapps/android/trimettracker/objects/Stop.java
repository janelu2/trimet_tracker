package com.beagleapps.android.trimettracker.objects;

public class Stop {
	private String mDesc;
	private String mStopID;
	
	Stop(){
		
	}
	
	public Stop(String desc, String stopID){
		setDesc(desc);
		setStopID(stopID);
	}

	public String getDesc() {
		return mDesc;
	}

	public void setDesc(String mDesc) {
		this.mDesc = mDesc;
	}

	public String getStopID() {
		return mStopID;
	}

	public void setStopID(String mStopID) {
		this.mStopID = mStopID;
	}
}
