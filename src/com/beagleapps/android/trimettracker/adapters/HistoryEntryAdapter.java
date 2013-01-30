package com.beagleapps.android.trimettracker.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.objects.HistoryEntry;
import com.beagleapps.android.trimettrackerfree.R;

public class HistoryEntryAdapter extends ArrayAdapter<HistoryEntry> {
	private final Activity context;
	private final ArrayList<HistoryEntry> items;

	public HistoryEntryAdapter(Activity context, ArrayList<HistoryEntry> items) {
		super(context, R.layout.favorite_stop_list_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView description;
		public TextView stopID;
		public TextView direction;
		public TextView routes;
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
			rowView = inflater.inflate(R.layout.favorite_stop_list_item, null, true);
			holder = new ViewHolder();
			holder.description = (TextView) rowView.findViewById(R.id.FSRouteDescription);
			holder.stopID = (TextView) rowView.findViewById(R.id.FSStopID);
			holder.direction = (TextView) rowView.findViewById(R.id.FSDirection);
			holder.routes = (TextView) rowView.findViewById(R.id.FSRoutes);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.description.setText(items.get(position).getDescription() + ":");
		holder.stopID.setText(Integer.toString(items.get(position).getStopID()));
		holder.direction.setText(items.get(position).getDirection() + ":");
		holder.routes.setText(items.get(position).getRoutes());

		return rowView;
	}
}