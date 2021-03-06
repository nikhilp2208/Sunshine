package com.example.android.sunshine;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.sync.SunshineSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {

    ForecastAdapter mForecastAdapter;
    String mPincode;
    String mTemp;
    SharedPreferences mPreferences;
    int mCursorCurrentPos;
    private boolean mUseTodayLayout;
    private static final int FORECAST_LOADER = 0;
    private static final String CURSOR_CURRENT_POS = "current_pos_cursor";

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public void onLocationChanged() {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            if(s == getString(R.string.pref_pincode_key)) {
                mPincode = Utility.getPreferredLocation(getActivity());
                updateWeather();
            }
            else if(s == getString(R.string.pref_temp_key)) {
                mTemp = mPreferences.getString(getString(R.string.pref_temp_key),getString(R.string.pref_temp_default));
            }
        }
    };

    private void updateWeather() {
        SunshineSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final Activity parentActivity = getActivity();
//        mForecastAdapter = new ArrayAdapter<String>(parentActivity,R.layout.list_item_forecast,R.id.list_item_forecast_textview,new ArrayList<String>());
//        String locationSetting = Utility.getPreferredLocation(getActivity());
//        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting,System.currentTimeMillis());
//        Cursor cur = getContext().getContentResolver().query(weatherForLocationUri,null,null,null,sortOrder);
        mForecastAdapter = new ForecastAdapter(getActivity(),null,0);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mForecastAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(CURSOR_CURRENT_POS)) {
            mCursorCurrentPos = savedInstanceState.getInt(CURSOR_CURRENT_POS);
        }

        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Toast.makeText(parentActivity, mWeatherArrayAdapter.getItem(i).toString(), Toast.LENGTH_SHORT).show();
                Cursor cursor = (Cursor)adapterView.getItemAtPosition(i);
                if (cursor != null) {
                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    mCursorCurrentPos = i;
                    ((Callback) getActivity()).onItemSelected(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, cursor.getLong(COL_WEATHER_DATE)));
                }
            }
        });
//        mPreferences = PreferenceManager.getDefaultSharedPreferences(parentActivity);
//        mPreferences.registerOnSharedPreferenceChangeListener(listener);
//        mPincode = mPreferences.getString(getString(R.string.pref_pincode_key), getString(R.string.pref_pincode_default));
//        mTemp = mPreferences.getString(getString(R.string.pref_temp_key),getString(R.string.pref_temp_default));
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_forecast_fragment,menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCursorCurrentPos != ListView.INVALID_POSITION) {
            outState.putInt(CURSOR_CURRENT_POS,mCursorCurrentPos);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        else if (id == R.id.action_show_on_map) {
            Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
            mPincode = Utility.getPreferredLocation(getActivity());
            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon().appendQueryParameter("q",mPincode).build();
            mapsIntent.setData(geoLocation);
            if (mapsIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivity(mapsIntent);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
//        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(locationSetting, System.currentTimeMillis());
        Uri uri = WeatherContract.WeatherEntry.buildWeatherLocation(locationSetting);
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        return new CursorLoader(getActivity(),uri,FORECAST_COLUMNS,null,null,sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v("ON_LOAD_FINISHED", Integer.toString(data.getCount()));
        mForecastAdapter.swapCursor(data);
        ListView listView = (ListView) getActivity().findViewById(R.id.listview_forecast);
        listView.smoothScrollToPosition(mCursorCurrentPos);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        mUseTodayLayout = useTodayLayout;
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(mUseTodayLayout);
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }
}
