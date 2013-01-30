package com.beagleapps.android.trimettracker.adapters;

import java.util.ArrayList;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.beagleapps.android.trimettracker.objects.Arrival;
import com.beagleapps.android.trimettrackerfree.Colors;
import com.beagleapps.android.trimettrackerfree.R;

public class ArrivalAdapter extends ArrayAdapter<Arrival> {
	private final Activity context;
	private final ArrayList<Arrival> items;

	public ArrivalAdapter(Activity context, ArrayList<Arrival> items) {
		super(context, R.layout.arrivals_list_item, items);
		this.context = context;
		this.items = items;
	}

	// static to save the reference to the outer class and to avoid access to
	// any members of the containing class
	static class ViewHolder {
		public TextView description;
		public TextView timeRemaining;
		public TextView scheduledTime;
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
			rowView = inflater.inflate(R.layout.arrivals_list_item, null, true);
			holder = new ViewHolder();
			holder.description = (TextView) rowView.findViewById(R.id.busDescription);
			holder.timeRemaining = (TextView) rowView.findViewById(R.id.arrivalTime);
			holder.scheduledTime = (TextView) rowView.findViewById(R.id.scheduledTime);
			rowView.setTag(holder);
		} else {
			holder = (ViewHolder) rowView.getTag();
		}

		holder.description.setText(items.get(position).getBusDescription() + ":");
		holder.timeRemaining.setText(items.get(position).getRemainingMinutes());
		String timeString = "Scheduled: " + items.get(position).getScheduledTimeText();
		holder.scheduledTime.setText(timeString);
		
		if(!items.get(position).isEstimated()){
			holder.timeRemaining.setTextColor(Color.RED);
		}
		else{
			// A different color green
			holder.timeRemaining.setTextColor(Color.parseColor(Colors.Green));
		}
		
		return rowView;
	}
}