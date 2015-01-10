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
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.Pair;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tsukurukai.gotoosanbashi.activities.HistoryListActivity;
import tsukurukai.gotoosanbashi.activities.MapsActivity;
import tsukurukai.gotoosanbashi.fragments.LoadingDialogFragment;
import tsukurukai.gotoosanbashi.models.CourseCalculator;
import tsukurukai.gotoosanbashi.models.Spot;

import static java.lang.Double.valueOf;


public class MainActivity extends FragmentActivity {

    private static final Integer NUMBER_OF_SPOTS = 4;

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
                                    ArrayList<Spot> spots = getSpots(location);
                                    Spot start = new Spot("現在地", location.getLatitude(), location.getLongitude());
                                    Spot goal  = new Spot(Const.GOAL_NAME, Const.GOAL_LATITUDE, Const.GOAL_LONGITUDE);

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

            final TextView continueTextView = (TextView)rootView.findViewById(R.id.link_history);
            onclickContinueTextView(continueTextView);

            return rootView;
        }

        private void onclickContinueTextView(TextView continueTextView) {

            continueTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = HistoryListActivity.createIntent(getActivity());
                    startActivity(intent);
                }
            });

        }
        private static final int MINIMUM_SPOT_COUNT = 3;

        private ArrayList<Spot> getSpots(Location location) {
            ArrayList<Spot> spots = new ArrayList<Spot>();

            String uri = makeSpotUri();

            Double currentLat = location.getLatitude();
            Double currentLng = location.getLongitude();
            Double goalLat = Const.GOAL_LATITUDE;
            Double goalLng = Const.GOAL_LONGITUDE;

            // 立ち寄り地の数分 request
            for (int i = 1; i < NUMBER_OF_SPOTS ; i++) {
                Pair<Double, Double> spotLatlng = Util.betweenLatLng(currentLat, currentLng, goalLat, goalLng, i, NUMBER_OF_SPOTS);
                Double spotLat = spotLatlng.first;
                Double spotLng = spotLatlng.second;

                String pageToken = ""; // pager

                int spotCount = 0;
                boolean hasNext = true;
                while( spotCount < MINIMUM_SPOT_COUNT || hasNext) {
                    String spotUri = uri + "&location=" + String.valueOf(spotLat) + ","
                            + String.valueOf(spotLng) + pageToken;
                    Log.d("spotURI", "spotURI: " + spotUri);
                    HttpGet request = new HttpGet(spotUri);
                    HttpResponse httpResponse;
                    HttpClient httpClient = new DefaultHttpClient();
                    try {
                        httpResponse = httpClient.execute(request);
                        int status = httpResponse.getStatusLine().getStatusCode();
                        if (HttpStatus.SC_OK == status) {
                            JSONObject json = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                            JSONArray results = json.getJSONArray("results");
                            if (json.has("next_page_token")) {
                                pageToken = "&pagetoken=" + json.getString("next_page_token");
                                spotCount += addSpots(spots, results);
                            } else {
                                hasNext = false;
                            }
                        } else {
                            Log.d("HttpStatus", "HTTP_Status: " + status);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            return spots;
        }

        private int addSpots(ArrayList<Spot> spots, JSONArray results) throws JSONException {
            int spotCount = 0;
            for (int j = 0; j < results.length(); j++) {
                JSONObject result = results.getJSONObject(j);
                JSONArray typeArray = result.getJSONArray("types");
                if (!isBlackListedSpot(typeArray)) {
                    spots.add(new Spot(result.getString("name"),
                            valueOf(result.getJSONObject("geometry").getJSONObject("location").getString("lat")),
                            valueOf(result.getJSONObject("geometry").getJSONObject("location").getString("lng"))));
                    spotCount++;
                }
            }
            return spotCount;
        }

        private String makeSpotUri() {
            String baseUri  = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
            String key      = "key=" + Secret.GOOGLE_PLACES_API_KEY;
            String whiteList = Const.SPOT_TYPE_WHITE_LIST;
            String radius   = "&radius=3000";
            String sensor   = "&sensor=false";
            String option   = "&language=ja";
            String uri = baseUri + key + radius + sensor + option;
            try {
                uri += "&types=" + URLEncoder.encode(whiteList, "utf-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            return uri;
        }

        /**
         * A method to check if a spot has a black-listed type
         * @return boolean
         */
        private boolean isBlackListedSpot(JSONArray typeArray) throws JSONException {
            String blackList = Const.SPOT_TYPE_BLACK_LIST;
            Pattern pattern = Pattern.compile(blackList);

            for (int i = 0; i < typeArray.length(); i++) {
                String type = (String) typeArray.get(i);
                Matcher m = pattern.matcher(type);
                if ( m.find() ) {
                    return true;
                }
            }
            return false;
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
