package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.adapters.DirectionAdapter;
import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;
import com.beagleapps.android.trimettracker.objects.Direction;
import com.beagleapps.android.trimettracker.R;

public class ChooseDirection extends Activity {
	
	private TextView vRouteDescription;
	private RoutesDocument mRoutesDocument = null;
	private ArrayList<Direction> mDirectionList;
	private ListView vDirectionListView;
	private ArrayList<Direction> mAdapterList;
	private DirectionAdapter mDirectionListAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosedirection);
		
		vRouteDescription = (TextView)findViewById(R.id.CD_RouteName);
		mRoutesDocument = new RoutesDocument();
		loadStopXML();

		vRouteDescription.setText(mRoutesDocument.getStopsRouteDescription());
		
		mDirectionList = new ArrayList<Direction>();
		
		vDirectionListView = (ListView)findViewById(R.id.CD_ListView);
		
		setupListeners();
		
		setupDirectionList();
	}
	
	private void loadStopXML() {
		RoutesDocument.mStopsXMLDoc = XMLIOHelper.loadFile(getBaseContext(), RoutesDocument.mStopsFileName);
	}

	
	private void setupListeners() {
		
		vDirectionListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onDirectionClick(mDirectionList.get(position).getDir());
			}
		});
	}

	protected void onDirectionClick(int dir) {
		
		RoutesDocument.setChosenDirection(dir);
		
		launchChooseStop(dir);
	}
	
	public void launchChooseStop(int dir) {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), ChooseStop.class);
		intent.putExtra("direction", dir);
		startActivity(intent);
	}
	
	private void setupDirectionList() {
		if (RoutesDocument.mStopsXMLDoc != null){
			int length = mRoutesDocument.getDirectionNodes().getLength();
			
			for (int index = 0; index < length; index++){
				String desc = mRoutesDocument.getDirDescription(index);
				
				mDirectionList.add(new Direction(desc, index));
			}
			mAdapterList = new ArrayList<Direction>(mDirectionList);
			mDirectionListAdapter = new DirectionAdapter(this, mAdapterList);
			
			vDirectionListView.setAdapter(mDirectionListAdapter);
			
		}
		else {
			showError(getString(R.string.errorGeneric));
		}
		
	}
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
}
