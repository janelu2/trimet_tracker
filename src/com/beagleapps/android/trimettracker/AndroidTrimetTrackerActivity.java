package com.beagleapps.android.trimettracker;

import android.app.Activity;
import android.os.Bundle;
import com.beagleapps.android.trimettrackerfree.R;

public class AndroidTrimetTrackerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}