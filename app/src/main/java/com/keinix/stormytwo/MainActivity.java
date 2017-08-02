package com.keinix.stormytwo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
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

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final String TAG = MainActivity.class.getSimpleName();
    private CurrentWeather mCurrentWeather;
    private FusedLocationProviderClient mFusedLocationClient;
    private double mUserLatitude;
    private double mUserLongitude;
    private String mLocationName;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;


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

        if (networkIsConnected()) {
            // this starts a chain reaction where the weather will be updated on screen
            // check the onConnected and onLocationChanged methods
            setLocationResources();
        } else {
            Toast.makeText(this, "Connect to the internet please", Toast.LENGTH_LONG).show();
        }

        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshWeather();
            }
        });

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


    private void setLocationResources() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = new LocationRequest()
                .setInterval(5000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mGoogleApiClient.connect();
    }


    private void refreshWeather() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleRefresh();
            }
        });

        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                parseLocation(location);
                getNewForecast();
            } else {
                // location handled + weather updated in onLocationChanged()
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }

        } catch (SecurityException e) {
            Log.e(TAG, "No permission for network location");
            Toast.makeText(this, R.string.location_warning, Toast.LENGTH_LONG).show();
        }
    }

    private void getNewForecast() {
        // makes a weather API call -> assigns data to model -> updates display
        // make sure to toggelRefresh() before calling this method
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleRefresh();
                    }
                });

                GenericAlertDialog.newInstance("Oops!", "Could not get weather data")
                        .show(getFragmentManager(), "NO_WEATHER_DATA");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        mCurrentWeather = getCurrentDetails(jsonData);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                                toggleRefresh();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Exception Caught", e);
                }
            }
        });
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

    private void  parseLocation(Location location) {
        mUserLatitude = location.getLatitude();
        mUserLongitude = location.getLongitude();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(mUserLatitude, mUserLongitude, 1);
            mLocationName = addresses.get(0).getLocality();
        } catch (IOException e) { e.printStackTrace(); }

    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        Log.d("FINDME", "onResume connect activated");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("FINDME", "The on connected method was activated");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                toggleRefresh();
            }
        });

        try {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (location != null) {
                Log.d("FINDME", "getLastLocation was null");
                parseLocation(location);
                getNewForecast();
            } else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
                Log.d("FINDME", "Location updates requested");
            }
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("FINDM", "onConnectionFailed method activated");

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("FINDME", "onLocationChanged activated");
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        parseLocation(location);
        getNewForecast();

    }
}
