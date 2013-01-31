package com.beagleapps.android.trimettracker.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.R;

public class AboutScreenAdapter extends ArrayAdapter<AboutItem> {
	private final Activity context;
	private final ArrayList<AboutItem> items;

	public AboutScreenAdapter(Activity context, ArrayList<AboutItem> items) {
		super(context, R.layout.about_screen_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public ImageView image;
		public TextView label;
		public TextView subtext;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ViewHolder will buffer the assess to the individual fields of the row
		// layout

		ViewHolder holder;
		// Recycle existing view if passed as parameter
		// This will save memory and time on Android
		// This only works if the base layout for all classes are the same
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.about_screen_item, null, true);
			holder = new ViewHolder();
			
			holder.image = (ImageView) rowView.findViewById(R.id.AS_item_image);
			holder.label = (TextView) rowView.findViewById(R.id.AS_item_label);
			holder.subtext = (TextView) rowView.findViewById(R.id.AS_item_subtext);
			
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}


		if (items.get(position).getType().equals("email")){
			holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.email));
		}
		else if (items.get(position).getType().equals("link")){
			holder.image.setImageDrawable(context.getResources().getDrawable(R.drawable.link));
		}
		
		holder.label.setText(items.get(position).getLabel());
		holder.subtext.setText(items.get(position).getSubtext());
		
		return rowView;
	}
}