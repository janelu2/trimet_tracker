package com.beagleapps.android.trimettracker.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.objects.Stop;
import com.beagleapps.android.trimettrackerfree.R;

public class StopAdapter extends ArrayAdapter<Stop> {
	private final Activity context;
	private final ArrayList<Stop> items;

	public StopAdapter(Activity context, ArrayList<Stop> items) {
		super(context, R.layout.choose_stop_list_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView description;
		public TextView stopID;
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
			rowView = inflater.inflate(R.layout.choose_stop_list_item, null, true);
			holder = new ViewHolder();
			holder.description = (TextView) rowView.findViewById(R.id.CS_StopDescription);
			holder.stopID = (TextView) rowView.findViewById(R.id.CS_StopID);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.description.setText(items.get(position).getDesc());
		holder.stopID.setText(items.get(position).getStopID());

		return rowView;
	}
}