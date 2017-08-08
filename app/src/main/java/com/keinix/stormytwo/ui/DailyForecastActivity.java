package com.keinix.stormytwo.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.keinix.stormytwo.R;
import com.keinix.stormytwo.adapters.DayAdapters;
import com.keinix.stormytwo.weather.Day;

import java.util.Arrays;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        Intent intent = getIntent();
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        DayAdapters adapter = new DayAdapters(this, mDays);
        setListAdapter(adapter);
    }
}
