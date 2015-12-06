package com.example.android.sunshine;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mWeatherArrayAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
        FetchWeatherClass fetchWeatherClass = new FetchWeatherClass();
        fetchWeatherClass.execute("560043");
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_forecast_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            FetchWeatherClass fetchWeatherClass = new FetchWeatherClass();
            fetchWeatherClass.execute("560043");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class FetchWeatherClass extends AsyncTask<String,Void,String[]> {

        public final String LOG_TAG = FetchWeatherClass.class.getSimpleName();

        private String getReadableDate(long time) {
            Date date = new Date(time*1000);
            DateFormat format = new SimpleDateFormat("E, MMM d");
            format.setTimeZone(TimeZone.getTimeZone("GMT+5:30"));
            return format.format(date);
        }

        private String getMinMaxTemp(double min,double max) {
            String maxMinTemp = Long.toString(Math.round(max)) + "/" + Long.toString(Math.round(min));
            return maxMinTemp;
        }

        private String[] getWeatherDataFromJson(String weatherJsonString, int numDays) throws JSONException{
            final String OWM_LIST = "list";
            final String OWM_DATE = "dt";
            final String OWM_TEMP = "temp";
            final String OWM_MIN = "min";
            final String OWM_MAX = "max";
            final String OWM_WEATHER = "weather";
            final String OWM_DESCRIPTION = "main";
            final String OWM_COUNT = "cnt";

            String[] weatherData = new String[numDays];

            JSONObject weatherDataObject = new JSONObject(weatherJsonString);
            int count = weatherDataObject.getInt(OWM_COUNT);
            JSONArray daysWeather = weatherDataObject.getJSONArray(OWM_LIST);

            for(int i = 0; i < numDays && i < count; i++) {
                JSONObject dayWeather = daysWeather.getJSONObject(i);
                JSONObject temp = dayWeather.getJSONObject(OWM_TEMP);
                JSONArray weatherArray = dayWeather.getJSONArray(OWM_WEATHER);
                JSONObject weather = weatherArray.getJSONObject(0);

                long timeStamp = dayWeather.getLong(OWM_DATE);
                String dateTime = getReadableDate(timeStamp);

                long min = temp.getLong(OWM_MIN);
                long max = temp.getLong(OWM_MAX);
                String maxMin = getMinMaxTemp(min, max);

                String description = weather.getString(OWM_DESCRIPTION);

                weatherData[i] = dateTime + " - " + description + " - " + maxMin;
            }
            return weatherData;
        }

        @Override
        protected String[] doInBackground(String... params) {

            HttpURLConnection httpURLConnection = null;
            BufferedReader bufferedReader = null;
            String forecastJsonString = null;
            String format = "json";
            int count = 7;
            String unit = "metric";
            try {
                final String baseUri = "http://api.openweathermap.org/data/2.5/forecast/daily";
                final String API_KEY = "c2dd03f27df90bcd4929f94d5835fc84";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNIT_PARAM = "units";
                final String COUNT_PARAM = "cnt";
                final String APPID_PARAM = "APPID";
                Uri uri = Uri.parse(baseUri).buildUpon().appendQueryParameter(QUERY_PARAM, params[0]).appendQueryParameter(FORMAT_PARAM, format).appendQueryParameter(UNIT_PARAM,unit).appendQueryParameter(COUNT_PARAM, Integer.toString(count)).appendQueryParameter(APPID_PARAM, API_KEY).build();
                URL url = new URL(uri.toString());
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                if(inputStream == null) {
                    return null;
                }
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                StringBuffer buffer = new StringBuffer();

                while((line = bufferedReader.readLine())!=null) {
                    buffer.append(line + "\n");
                }

                if(buffer.length()==0) {
                    return null;
                }

                forecastJsonString = buffer.toString();

                try {
                    String[] weatherData = getWeatherDataFromJson(forecastJsonString,count);
                    return weatherData;
                } catch (JSONException e){
                    Log.e(LOG_TAG,e.getMessage(),e);
                    e.printStackTrace();
                    return null;
                }

            } catch (IOException e){
                Log.e(LOG_TAG,"Error ",e);
                return null;
            } finally {
                if(httpURLConnection != null) {
                    httpURLConnection.disconnect();
                }

                if(bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG,"Error ",e);
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);
            if(result!=null){
                mWeatherArrayAdapter.clear();
                mWeatherArrayAdapter.addAll(result);
            }
        }
    }
}
