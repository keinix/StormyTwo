package com.keinix.stormytwo.ui;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.keinix.stormytwo.R;
import com.keinix.stormytwo.adapters.DayAdapters;
import com.keinix.stormytwo.weather.Day;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DailyForecastActivity extends ListActivity {
    private Day[] mDays;
    private String mLocationName;

    @BindView(R.id.locationLabel) TextView locationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_forecast);
        ButterKnife.bind(this);


        Intent intent = getIntent();
        mLocationName = intent.getStringExtra("locationName");
        Parcelable[] parcelables = intent.getParcelableArrayExtra(MainActivity.DAILY_FORECAST);
        mDays = Arrays.copyOf(parcelables, parcelables.length, Day[].class);

        locationLabel.setText(mLocationName);

        DayAdapters adapter = new DayAdapters(this, mDays);
        setListAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        String dayOfTheWeek = mDays[position].getDayOfTheWeek();
        String conditions = mDays[position].getSummary();
        String tempMax = mDays[position].getTemperatureMax() + "";
        String messege = String.format("On %s the high will be %s and it will be %s",
                dayOfTheWeek, tempMax, conditions);

        Toast.makeText(this, messege, Toast.LENGTH_LONG).show();

    }
}
