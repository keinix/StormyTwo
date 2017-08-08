package com.keinix.stormytwo.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.keinix.stormytwo.R;
import com.keinix.stormytwo.weather.Day;

public class DayAdapters extends BaseAdapter {
    private Context mContext;
    private Day[] mDays;

    public DayAdapters(Context context, Day[] days) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int i) {
        return mDays[i];
    }

    @Override
    public long getItemId(int i) {
        // con be used to tag items for easy reference
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            // this means it's a brand new view
            view = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();

            holder.iconImageView = view.findViewById(R.id.iconImageView);
            holder.temperatureLabel = view.findViewById(R.id.temperatureLabel);
            holder.dayLabel = view.findViewById(R.id.dayNameLabel);
            holder.circleImageView = view.findViewById(R.id.circleImageView);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Day day = mDays[i];

        holder.iconImageView.setImageResource(day.getIconID());
        holder.temperatureLabel.setText(Integer.toString(day.getTemperatureMax()));
        holder.dayLabel.setText(day.getDayOfTheWeek());
        holder.circleImageView.setImageResource(R.drawable.bg_temperature);

        return view;
    }

    @Override
    public CharSequence[] getAutofillOptions() {
        return new CharSequence[0];
    }

    private static class ViewHolder {
        ImageView iconImageView;
        ImageView circleImageView;
        TextView temperatureLabel;
        TextView dayLabel;
    }
}
