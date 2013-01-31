package com.beagleapps.android.trimettracker;

import java.net.MalformedURLException;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public abstract class DownloadXMLAsyncTask<ActivityType> extends AsyncTask<String, Void, XMLHandler> {
	protected ActivityType activity = null;
	protected boolean isDone = false;
	protected final String TAG = "DownloadXMLAsyncTask";
	private Context mContext;

	DownloadXMLAsyncTask() {
		
	}
	
	DownloadXMLAsyncTask(ActivityType activity, Context context) {
		attach(activity, context);
	}
	
	public void attach(ActivityType activity, Context context) {
		this.activity = activity;
		mContext = context;
	}

	public void detach() {
		activity = null;
		mContext = null;
	}

	public boolean isDone() {
		return isDone;
	}

	protected XMLHandler doInBackground(String... urls) {
		XMLHandler newXmlHandler = null;
		try {
			newXmlHandler = new XMLHandler(urls[0], mContext, getFileName());
			newXmlHandler.refreshXmlData();
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
		}
		return newXmlHandler;
	}

	// These two methods are implemented by each view that uses this class.
	abstract protected void onPreExecute();

	abstract protected void onPostExecute(XMLHandler newXmlHandler);
	
	abstract protected String getFileName();
	
	@Override
    protected void onCancelled() {
        isDone = true;
    }
}

