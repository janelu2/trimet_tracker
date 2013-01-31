package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.objects.Route;
import com.beagleapps.android.trimettrackerfree.R;

public class FindNearbyPopup extends Activity {
	
	private ArrayList<String> mRoutes;
	
	private ListView vRouteList;
	private TextView vStopLabel;
	private TextView vDirection;
	private Button vShowStopButton;
	
	private PopupOverlayItem mPopupItem;

	private DownloadArrivalDataTask mDownloadArrivalTask = null;
	private ProgressDialog mArrivalsDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.find_nearby_popup);
		
		mRoutes = new ArrayList<String>();
		vRouteList = (ListView)findViewById(R.id.FNP_routes_list);
		vStopLabel = (TextView)findViewById(R.id.FNP_stop_label);
		vDirection = (TextView)findViewById(R.id.FNP_direction_label);
		vShowStopButton = (Button)findViewById(R.id.FNP_show_stop_button);
		
		mPopupItem = new PopupOverlayItem();
		
		vStopLabel.setText(PopupOverlayItem.getOverlayItem().getTitle());
		vDirection.setText(PopupOverlayItem.getOverlayItem().getSnippet());
		
		setupListeners();
		
		setupRouteList();
		
		vRouteList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mRoutes));
	}
	


	private void setupRouteList() {
		ArrayList<Route> routes = PopupOverlayItem.getRouteList(); 
		for(Route route : routes){
			mRoutes.add(route.getDesc());
		}
	}

	private void setupListeners() {
		
		vShowStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onShowStopButtonClick();
			}
		});
	}
	
	private void onShowStopButtonClick() {
		String urlString = new String(getString(R.string.baseArrivalURL)+ PopupOverlayItem.getStopID());


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadArrivalTask = new DownloadArrivalDataTask(this);
			mDownloadArrivalTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}
	}
	
	private void setupArrivalsDialog(){
		mArrivalsDialog = null;
		mArrivalsDialog = new ProgressDialog(this);
		mArrivalsDialog.setMessage(getString(R.string.dialogGettingArrivals));
		mArrivalsDialog.setIndeterminate(true);
		mArrivalsDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				mDownloadArrivalTask.cancel(true);
			}
		};
		
		mArrivalsDialog.setOnCancelListener(onCancelListener);
	}
	
	void showArrivalsDialog(){
		setupArrivalsDialog();
		mArrivalsDialog.show();
	}

	void dismissArrivalsDialog(){
		if (mArrivalsDialog != null){
			mArrivalsDialog.dismiss();
		}
	}
	
	protected void launchShowStop() {
		Intent showStopIntent = new Intent();
		showStopIntent.setClass(getApplicationContext(), ShowStop.class);
		startActivity(showStopIntent);
	}
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
	
	class DownloadArrivalDataTask extends DownloadXMLAsyncTask<FindNearbyPopup> {
		DownloadArrivalDataTask(FindNearbyPopup activity) {
			super(activity, getApplicationContext());
		}

		protected void onPreExecute() {
			isDone = false;
			
			if (activity != null){
				activity.showArrivalsDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;
			
			if (activity != null){
				activity.dismissArrivalsDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					ArrivalsDocument.mXMLDoc = newXmlHandler.getXmlDoc();
					ArrivalsDocument.mRequestTime = newXmlHandler.getRequestTime();
					
					activity.launchShowStop();
				}
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		@Override
		protected String getFileName() {
			return ArrivalsDocument.mFileName;
		}
	}
}
