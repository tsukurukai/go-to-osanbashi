package tsukurukai.gotoosanbashi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.activities.MapsActivity;
import tsukurukai.gotoosanbashi.fragments.LoadingDialogFragment;
import tsukurukai.gotoosanbashi.models.CourseCalculator;
import tsukurukai.gotoosanbashi.models.Spot;

import static java.lang.Double.valueOf;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final TextView textView = (TextView)rootView.findViewById(R.id.link_map);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.newInstance();
                    if (loadingDialogFragment.getDialog() == null || !loadingDialogFragment.getDialog().isShowing()) {
                        loadingDialogFragment.show(getActivity().getFragmentManager(), "loadingDialog");
                    }

                    currentLocationExecute(new LocationListener() {
                        @Override
                        public void onLocationChanged(final Location location) {

                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    ArrayList<Spot> spots = getPlaces();
                                    Spot start = new Spot("現在地", location.getLatitude(), location.getLongitude());
                                    Spot goal = new Spot("大桟橋", 35.451762, 139.647758);

                                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);

                                    for (int i = 0; i < 3; i++) {
                                        List<Spot> course = CourseCalculator.calculate(spots, 3, start, goal);
                                        for (int j = 0; j < course.size(); j++) {
                                            sharedPreferences
                                            .edit()
                                            .putString("course:" + i + ":spots:" + j, course.get(j).toJson())
                                            .apply();
                                        }
                                        sharedPreferences
                                            .edit()
                                            .putInt("course:" + i + ":spotsCount", course.size())
                                            .commit();
                                    }

                                    if (loadingDialogFragment != null && loadingDialogFragment.getDialog() != null) {
                                        loadingDialogFragment.getDialog().dismiss();
                                    }
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    super.onPostExecute(aVoid);
                                    Intent intent = MapsActivity.createIntent(getActivity());
                                    startActivity(intent);
                                }
                            }.execute();
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {

                        }

                        @Override
                        public void onProviderEnabled(String provider) {

                        }

                        @Override
                        public void onProviderDisabled(String provider) {

                        }
                    });
                }
            });
            return rootView;
        }

        private ArrayList<Spot> getPlaces() {
            //String uri = "http://techbooster.org/feed/";
            String baseUri  = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            String key      = "key=" + Secret.GOOGLE_PLACES_API_KEY;
            String location = "&location=35.454721,139.631666";
            String types    = "&types=food";
            String radius   = "&radius=500";
            String sensor   = "&sensor=false";
            String uri = baseUri + key + location + radius + types + sensor;
            HttpGet request = new HttpGet(uri);
            HttpResponse httpResponse;
            HttpClient httpClient = new DefaultHttpClient();
            ArrayList<Spot> spots = new ArrayList<Spot>();
            try {
                httpResponse = httpClient.execute(request);
                int status = httpResponse.getStatusLine().getStatusCode();
                if (HttpStatus.SC_OK == status) {
                    JSONObject json = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                    JSONArray results = json.getJSONArray("results");
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject jsonObject = results.getJSONObject(i);
                        spots.add(new Spot(jsonObject.getString("name"),
                                valueOf(jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat")),
                                valueOf(jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng"))));
                        Log.d("json_name", jsonObject.getString("name"));
                        Log.d("json_latitude", jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lat"));
                        Log.d("json_longitude", jsonObject.getJSONObject("geometry").getJSONObject("location").getString("lng"));
                    }
                } else {
                    Log.d("HttpStatus", "Status" + status);
                }
            } catch ( Exception e) {
                throw new RuntimeException(e);
            }
            return spots;
        }

        private void currentLocationExecute(LocationListener listener) {
            LocationManager locationManager =
                    (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
            final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!gpsEnabled) {
                Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(settingsIntent);
            } else {
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(false);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setSpeedRequired(false);
                locationManager.requestSingleUpdate(
                        criteria,
                        listener,
                        null
                );
            }
        }
    }

}
