package com.beagleapps.android.trimettracker;


public class HomepageRotationInstance {
	private MainView.DownloadArrivalDataTask mDownloadArrivalTask = null;
	private MainView.DownloadRoutesDataTask mDownloadRoutesTask = null;
	
	public HomepageRotationInstance(MainView.DownloadArrivalDataTask mDownloadArrivalTask,
			MainView.DownloadRoutesDataTask mDownloadRoutesTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
		this.mDownloadRoutesTask = mDownloadRoutesTask;
	}
	
	public HomepageRotationInstance() {
		this.mDownloadArrivalTask = null;
		this.mDownloadRoutesTask = null;
	}

	public MainView.DownloadArrivalDataTask getDownloadArrivalTask() {
		return mDownloadArrivalTask;
	}
	
	public void setDownloadArrivalTask(MainView.DownloadArrivalDataTask mDownloadArrivalTask) {
		this.mDownloadArrivalTask = mDownloadArrivalTask;
	}
	
	public MainView.DownloadRoutesDataTask getDownloadRoutesTask() {
		return mDownloadRoutesTask;
	}
	
	public void setDownloadRoutesTask(MainView.DownloadRoutesDataTask mDownloadRoutesTask) {
		this.mDownloadRoutesTask = mDownloadRoutesTask;
	}
	

}
