package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.adapters.DetourAdapter;
import com.beagleapps.android.trimettracker.helpers.XMLIOHelper;
import com.beagleapps.android.trimettracker.objects.Detour;
import com.beagleapps.android.trimettrackerfree.R;

public class ShowDetour extends Activity {
	String TAG = "showDetour";

	private ArrayList<Detour> mDetours = null;
	private DetourAdapter DetourAdapter;

	private TextView vTitle;
	private ListView vDetoursListView;
	private DetoursDocument mDetoursDoc;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.showdetours);

		vDetoursListView = (ListView)findViewById(R.id.SD_DetoursListView);
		vTitle = (TextView)findViewById(R.id.SD_Title);

		mDetours = new ArrayList<Detour>();
		mDetoursDoc = new DetoursDocument();
		loadStopXML();

		vTitle.setText("Active Detours");

		getDetours();

		DetourAdapter = new DetourAdapter(this, mDetours);
		vDetoursListView.setAdapter(DetourAdapter);
		
		refreshDetourList();

	}

	private void loadStopXML() {
		DetoursDocument.mXMLDoc = XMLIOHelper.loadFile(getBaseContext(), DetoursDocument.mFileName);
	}

	private void refreshDetourList() {
		getDetours();
		DetourAdapter.notifyDataSetChanged();
	}

	private void getDetours() {
		mDetours.clear();
		int length = mDetoursDoc.length();
		
		for (int index = 0; index < length; index++){
			Detour detour = new Detour();
			detour.setDesc(mDetoursDoc.getDetourDescription(index));
			detour.setRoutes("Routes: " + mDetoursDoc.getRoutesString(index));
			
			mDetours.add(detour);
		}
	}

	
}
