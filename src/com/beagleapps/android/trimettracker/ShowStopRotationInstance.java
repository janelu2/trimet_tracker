package com.beagleapps.android.trimettracker;


import com.beagleapps.android.trimettracker.ShowStop.DownloadArrivalDataTask;
import com.beagleapps.android.trimettracker.ShowStop.DownloadDetourDataTask;




public class ShowStopRotationInstance {
	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private DownloadDetourDataTask mDownloadDetourTask = null;
	
	public ShowStopRotationInstance(DownloadArrivalDataTask mDownloadArrivalTask,
			DownloadDetourDataTask mDownloadDetoursTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
		this.mDownloadDetourTask = mDownloadDetoursTask;
	}
	
	public ShowStopRotationInstance() {
		this.mDownloadArrivalTask = null;
		this.mDownloadDetourTask = null;
	}

	public DownloadArrivalDataTask getDownloadArrivalTask() {
		return mDownloadArrivalTask;
	}
	
	public void setDownloadArrivalTask(DownloadArrivalDataTask mDownloadArrivalTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
	}
	
	public DownloadDetourDataTask getDownloadDetoursTask() {
		return mDownloadDetourTask;
	}
	
	public void setDownloadDetoursTask(DownloadDetourDataTask mDownloadDetoursTask) {
		this.mDownloadDetourTask = mDownloadDetoursTask;
	}
	

}
