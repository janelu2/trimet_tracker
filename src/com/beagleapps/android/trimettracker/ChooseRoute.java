package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.adapters.RouteAdapter;
import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;
import com.beagleapps.android.trimettracker.objects.Route;
import com.beagleapps.android.trimettrackerfree.R;

public class ChooseRoute extends Activity {
	
	private DownloadRoutesDataTask mDownloadRoutesDataTask = null;
	private ProgressDialog mDialog;
	private ListView vRouteListView;
	private TextView vRouteFilterTextView;
	private RouteAdapter mRouteListAdapter;
	private RoutesDocument mRoutesDocument = null;
	private ArrayList<Route> mRouteList;

	private ArrayList<Route> mAdapterList;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.chooseroute);
		
		mRoutesDocument = new RoutesDocument();
		loadStopXML();
		mRouteList = new ArrayList<Route>();
		
		vRouteListView = (ListView)findViewById(R.id.CR_ListView);
		vRouteFilterTextView = (TextView)findViewById(R.id.CR_FilterTextBox);
		
		setupListeners();
		
		setupRouteList();
		
		handleRotation();

	}
	
	private void loadStopXML() {
		RoutesDocument.mRouteXMLDoc = XMLIOHelper.loadFile(getBaseContext(), RoutesDocument.mRoutesFileName);
	}

	private void handleRotation() {
		mDownloadRoutesDataTask = (DownloadRoutesDataTask)getLastNonConfigurationInstance();
		
		if (mDownloadRoutesDataTask != null){
			mDownloadRoutesDataTask.attach(this, getApplicationContext());
			if (mDownloadRoutesDataTask.isDone()){
				dismissDialog();
			}
			else{
				showDialog();
			}
		}
	}
	
	private void setupListeners() {
		
		vRouteListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onRouteClick(mAdapterList.get(position).getNumber());
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

	protected void onRouteClick(String routeNumber) {
		String urlString = new String(getString(R.string.baseRoutesURL) + 
				getString(R.string.endRoutesURL) + routeNumber);


		if (Connectivity.checkForInternetConnection(getApplicationContext()))
		{
			mDownloadRoutesDataTask = new DownloadRoutesDataTask(this);
			mDownloadRoutesDataTask.execute(urlString);
		}
		else{
			Connectivity.showErrorToast(getApplicationContext());
		}	
	}

	protected void onFilterTextChange() {
		mAdapterList.clear();
		for (Route route : mRouteList) {
			if (route.getDesc().toLowerCase().contains(vRouteFilterTextView.getText().toString().toLowerCase())){
				mAdapterList.add(route);
			}
		}
		
		mRouteListAdapter.notifyDataSetChanged();
		
		if (mAdapterList.isEmpty()){
			// TODO show a message
		}
	}

	private void setupRouteList() {
		if (RoutesDocument.mRouteXMLDoc != null){
			int length = mRoutesDocument.getRoutesNodes().getLength();
			
			for (int index = 0; index < length; index++){
				String desc = mRoutesDocument.getRouteDescription(index);
				String number = mRoutesDocument.getRouteNumber(index);
				
				if (!RoutesHelper.isExcludedRoute(number)){
					mRouteList.add(new Route(desc, number));
				}
			}
			mAdapterList = new ArrayList<Route>(mRouteList);
			mRouteListAdapter = new RouteAdapter(this, mAdapterList);
			
			vRouteListView.setAdapter(mRouteListAdapter);
			
		}
		else {
			showError(getString(R.string.errorGeneric));
		}
		
	}

	private void setupDialog(){
		mDialog = null;
		mDialog = new ProgressDialog(this);
		mDialog.setMessage(getString(R.string.dialogGettingRoutes));
		mDialog.setIndeterminate(true);
		mDialog.setCancelable(true);
		
		OnCancelListener onCancelListener = new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				ChooseRoute.this.mDownloadRoutesDataTask.cancel(true);
			}
		};
		
		mDialog.setOnCancelListener(onCancelListener);
	}
	
	public void launchChooseDirection() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ChooseDirection.class);
		startActivity(intent);
	}

	private void showDialog(){
		setupDialog();
		mDialog.show();
	}

	private void dismissDialog(){
		mDialog.dismiss();
	}
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
		if (mDownloadRoutesDataTask != null){
			mDownloadRoutesDataTask.detach();
		}

		return(mDownloadRoutesDataTask);
	}

	private class DownloadRoutesDataTask extends DownloadXMLAsyncTask<ChooseRoute> {
		
		DownloadRoutesDataTask(ChooseRoute activity) {
			super(activity, getApplicationContext());
		}

		protected void onPreExecute() {
			isDone = false;

			if (activity != null){
				activity.showDialog();
			}
			else{
				Log.w(TAG, "showStop activity is null");
			}
		}

		protected void onPostExecute(XMLHandler newXmlHandler) {
			isDone = true;

			if (activity != null){
				activity.dismissDialog();
				if (newXmlHandler.hasError())
					activity.showError(newXmlHandler.getError());
				else{
					RoutesDocument.mStopsXMLDoc = newXmlHandler.getXmlDoc();
					activity.launchChooseDirection();
				}
			}
			else{
				Log.w(TAG, "findRoute activity is null");
			}
		}

		@Override
		protected String getFileName() {
			return RoutesDocument.mStopsFileName;
		}
	}
}
