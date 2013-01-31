package com.beagleapps.android.trimettracker.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.objects.Detour;
import com.beagleapps.android.trimettracker.R;

public class DetourAdapter extends ArrayAdapter<Detour> {
	private final Activity context;
	private final ArrayList<Detour> items;

	public DetourAdapter(Activity context, ArrayList<Detour> items) {
		super(context, R.layout.detours_list_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView description;
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
			rowView = inflater.inflate(R.layout.detours_list_item, null, true);
			holder = new ViewHolder();
			holder.description = (TextView) rowView.findViewById(R.id.SD_description);
			holder.routes = (TextView) rowView.findViewById(R.id.SD_routes);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.routes.setText(items.get(position).getRoutes());
		holder.description.setText(items.get(position).getDesc());
		
		
		return rowView;
	}
}