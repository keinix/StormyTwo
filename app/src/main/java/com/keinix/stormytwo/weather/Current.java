package com.keinix.stormytwo.weather;


import com.keinix.stormytwo.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Current {
    private String mIcon;
    private long mTime;
    private double mTemperature;
    private double mHumidity;
    private double mPercipChance;
    private String mSummary;
    private String mTimeZone;

    public String getmIcon() {
        return mIcon;
    }

    public void setmIcon(String mIcon) {
        this.mIcon = mIcon;
    }

    public int getIconId() { return Forecast.getIconID(mIcon); }

    public long getmTime() {
        return mTime;
    }

    public String getFormattedTime() {

        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone(mTimeZone));
        Date dateTime = new Date(mTime * 1000);
        return formatter.format(dateTime);
    }

    public String getmTimeZone() {
        return mTimeZone;
    }

    public void setmTimeZone(String mTimeZone) {
        this.mTimeZone = mTimeZone;
    }

    public void setmTime(long mTime) {
        this.mTime = mTime;
    }

    public int getmTemperature() {
        return (int) Math.round(mTemperature);
    }

    public void setmTemperature(double mTemperature) {
        this.mTemperature = mTemperature;
    }

    public double getmHumidity() {
        return mHumidity;
    }

    public void setmHumidity(double mHumidity) {
        this.mHumidity = mHumidity;
    }

    public int getmPercipChance() {
        return (int) mPercipChance * 100;
    }

    public void setmPercipChance(double mPercipChance) {
        this.mPercipChance = mPercipChance;
    }

    public String getmSummary() {
        return mSummary;
    }

    public void setmSummary(String mSummary) {
        this.mSummary = mSummary;
    }
}
