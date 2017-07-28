package com.keinix.stormytwo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    private FusedLocationProviderClient mFusedLocationClient;
    private double mUserLatitude;
    private double mUserLongitude;
    private String mLocationName;


    @BindView(R.id.timeLabel) TextView mTimeLabel;
    @BindView(R.id.temperatureLabel) TextView mTemperatureLabel;
    @BindView(R.id.humidityValue) TextView mHumidityValue;
    @BindView(R.id.precipValue) TextView mPrecipValue;
    @BindView(R.id.summaryLabel) TextView mSummaryLabel;
    @BindView(R.id.iconImageView) ImageView mIconImageView;
    @BindView(R.id.refreshImageView) ImageView mRefreshImageView;
    @BindView(R.id.progressBar) ProgressBar mProgressBar;
    @BindView(R.id.locationLabel) TextView mLocationLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setVisibility(View.INVISIBLE);



        if (Build.VERSION.SDK_INT >= 23) {
            locationPermissionRequest();

        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getForcast();
        }

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getForcast();
            }
        });

    }

    private void getForcast() {


        if (networkIsConnected()) {

            toggleRefresh();
            getUserLocation();

        } else {
            GenericAlertDialog.newInstance("Oops!", "GET ON THE INTERNET NIGGA")
                    .show(getFragmentManager(), "NotConnected");
        }
    }

    private void getUserLocation() {
        try {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            mUserLatitude = location.getLatitude();
                            mUserLongitude = location.getLongitude();
                            getLocationName();
                            weatherApiCall();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "No permission for getwork location");
            Toast.makeText(this, R.string.location_warning, Toast.LENGTH_LONG).show();
        }
    }

    private void getLocationName() {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mUserLatitude, mUserLongitude, 1);
            mLocationName = addresses.get(0).getLocality();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void weatherApiCall() {
        String apiKey ="4ada4e54ba9b437077c6695a8cdb202a";
        String forecastUrl = "https://api.darksky.net/forecast/" +
                apiKey + "/" + mUserLatitude + "," + mUserLongitude;


        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(forecastUrl)
                .build();

        Call call = client.newCall(request);

        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                toggleRefresh();
                GenericAlertDialog.newInstance("Oops!", "Could not get eather data")
                        .show(getFragmentManager(), "NO_WEATHER_DATA");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleRefresh();
                    }
                });
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception Caught", e);
                }
            }
        });
    }

    private void locationPermissionRequest() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 123);

        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            getForcast();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                    getForcast();

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Toast.makeText(this, "Please enable location access to use this app",
                            Toast.LENGTH_LONG).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private boolean networkIsConnected() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo= manager.getActiveNetworkInfo();
        boolean isConneted = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isConneted = true;
        }
        return isConneted;
    }

    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException {
        JSONObject forecast = new JSONObject(jsonData);
        JSONObject currently = forecast.getJSONObject("currently");

        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setmTimeZone(forecast.getString("timezone"));
        currentWeather.setmHumidity(currently.getDouble("humidity"));
        currentWeather.setmIcon(currently.getString("icon"));
        currentWeather.setmPercipChance(currently.getDouble("precipProbability"));
        currentWeather.setmSummary(currently.getString("summary"));
        currentWeather.setmTemperature(currently.getDouble("temperature"));
        currentWeather.setmTime(currently.getLong("time"));

        Log.d(TAG, currentWeather.getFormattedTime());

        return currentWeather;
    }

    private void updateDisplay() {
        mTemperatureLabel.setText(mCurrentWeather.getmTemperature() + "");
        mTimeLabel.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getmHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getmPercipChance() + "%");
        mSummaryLabel.setText(mCurrentWeather.getmSummary());
        mLocationLabel.setText(mLocationName);


        Drawable drawable = ContextCompat.getDrawable(this, mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }


}
