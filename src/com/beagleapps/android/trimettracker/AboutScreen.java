package com.beagleapps.android.trimettracker;

import java.util.ArrayList;

import com.beagleapps.android.trimettrackerfree.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.beagleapps.android.trimettracker.adapters.AboutItem;
import com.beagleapps.android.trimettracker.adapters.AboutScreenAdapter;

public class AboutScreen extends Activity {
	
	private ArrayList<AboutItem> mAboutItems;
	private AboutScreenAdapter mAboutAdapter;
	
	private ListView vAboutListView;
	private TextView vVersionText;
	private TextView vCopyrightText;
	private ImageView vAppIcon;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about_screen);
		
		mAboutItems = new ArrayList<AboutItem>();
		vAboutListView = (ListView)findViewById(R.id.AS_link_list);
		vCopyrightText = (TextView)findViewById(R.id.AS_copyright_text);
		vVersionText = (TextView)findViewById(R.id.AS_version_text);
		vAppIcon = (ImageView)findViewById(R.id.AS_app_icon);
		
		setupListeners();
		
		setupAboutList();
		setupCopyrightBox();
		
		mAboutAdapter = new AboutScreenAdapter(this, mAboutItems);
		vAboutListView.setAdapter(mAboutAdapter);
	}
	
	private void setupCopyrightBox() {
		vAppIcon.setImageDrawable(getResources().getDrawable(R.drawable.icon));
		vCopyrightText.setText("� Copyright 2012 " + getString(R.string.authorName));

		String version = ManifestHelper.getCurrentVersion(this);
		
		
		vVersionText.setText("Version: " + version + " by " + getString(R.string.companyName));
		
	}

	private void setupAboutList() {
		// Get JSON string from file
		String jsonString = JSONReader.readAsset("about_items.json", this);
		
		
		
		try {
			JSONObject jsonObject = new JSONObject(jsonString);
			JSONArray linksArray = jsonObject.getJSONObject("about").getJSONArray("links");
			
			for (int index = 0; index < linksArray.length(); index++){
				AboutItem newItem = new AboutItem();
				newItem.setLabel(linksArray.getJSONObject(index).getString("label").toString());
				newItem.setSubtext(linksArray.getJSONObject(index).getString("subtext").toString());
				newItem.setType(linksArray.getJSONObject(index).getString("type").toString());
				newItem.setData(linksArray.getJSONObject(index).getString("data").toString());
				mAboutItems.add(newItem);
			}
			
		} catch (JSONException e) {
			showError("Error: " + e.getMessage());
		}
	}

	private void setupListeners() {
		
		vAboutListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> a, View v, int position, long id) {
				onAboutItemClick(mAboutItems.get(position));
			}
		});
	}
	
	private void onAboutItemClick(AboutItem aboutItem) {
		if (aboutItem.getType().equals("link")){
			String url = aboutItem.getData();
			Intent intent = new Intent(Intent.ACTION_VIEW);  
			intent.setData(Uri.parse(url)); 
			startActivity(intent);  
		}
		else if (aboutItem.getType().equals("email")){
			Intent i = new Intent(Intent.ACTION_SEND);
			i.putExtra(Intent.EXTRA_EMAIL, new String[] {aboutItem.getData()});

			i.putExtra(android.content.Intent.EXTRA_SUBJECT, "Trimet Tracker Android: Support/Feedback" );
			
			startActivity(Intent.createChooser(i, "Send email"));
		}
		
	}
	
	protected void showError(String error) {
		Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
	}
}
