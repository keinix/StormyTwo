package com.keinix.stormytwo.ui;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

import com.keinix.stormytwo.R;
import com.keinix.stormytwo.adapters.DayAdapters;
import com.keinix.stormytwo.weather.Day;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);

        DayAdapters adapter = new DayAdapters(this, mDays);
    }
}
