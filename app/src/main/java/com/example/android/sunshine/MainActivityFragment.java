package com.example.android.sunshine;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    ArrayAdapter<String> mWeatherArrayAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] data = {"Monday - Sunny","Tuesday - Sunny","Wed - Rains","Thur - Rains","Fri - Rains","Sat - Rains","Sun - Rains"};
        List<String> weatherList = new ArrayList<String>(Arrays.asList(data));
        Activity parentActivity = getActivity();
        mWeatherArrayAdapter = new ArrayAdapter<String>(parentActivity,R.layout.list_item_forecast,R.id.list_item_forecast_textview,weatherList);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mWeatherArrayAdapter);
        return rootView;
    }
}
