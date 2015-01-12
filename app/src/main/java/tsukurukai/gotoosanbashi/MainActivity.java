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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.activities.HistoryListActivity;
import tsukurukai.gotoosanbashi.activities.MapsActivity;
import tsukurukai.gotoosanbashi.fragments.LoadingDialogFragment;
import tsukurukai.gotoosanbashi.models.CourseCalculator;
import tsukurukai.gotoosanbashi.models.Spot;


public class MainActivity extends FragmentActivity {

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
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private TextView linkMapTextView;
        private TextView linkHistoryTextView;

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            linkMapTextView = (TextView)rootView.findViewById(R.id.link_map);
            linkMapTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final LoadingDialogFragment loadingDialogFragment = LoadingDialogFragment.newInstance();
                    if (loadingDialogFragment.getDialog() == null || !loadingDialogFragment.getDialog().isShowing()) {
                        loadingDialogFragment.show(getActivity().getFragmentManager(), "loadingDialog");
                    }

                    currentLocationExecute(new LocationListener() {
                        @Override
                        public void onLocationChanged(final Location location) {

                            new AsyncTask<Void, Void, ArrayList<Spot>>() {
                                @Override
                                protected ArrayList<Spot> doInBackground(Void... params) {
                                    ArrayList<Spot> spots;
                                    try {
                                        spots = Spot.findByLocation(location);
                                    } catch (IOException | JSONException e) {
                                        spots = null;
                                    }
                                    return spots;
                                }

                                @Override
                                protected void onPostExecute(ArrayList<Spot> spots) {
                                    if (loadingDialogFragment != null && loadingDialogFragment.getDialog() != null) {
                                        loadingDialogFragment.getDialog().dismiss();
                                    }

                                    if (spots != null && spots.size() > 0) {
                                        Spot start = new Spot(getResources().getString(R.string.text_your_location), location.getLatitude(), location.getLongitude());
                                        Spot goal = new Spot(getResources().getString(R.string.text_osanbashi), Const.GOAL_LATITUDE, Const.GOAL_LONGITUDE);

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

                                        Intent intent = MapsActivity.createIntent(getActivity());
                                        startActivity(intent);

                                    } else {
                                        Toast.makeText(
                                                getActivity(),
                                                getResources().getString(R.string.error_msg_cannot_connect_server),
                                                Toast.LENGTH_LONG).show();
                                    }
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

            linkHistoryTextView = (TextView)rootView.findViewById(R.id.link_history);
            onclickContinueTextView(linkHistoryTextView);

            TextView versionTextView = (TextView) rootView.findViewById(R.id.text_version);
            versionTextView.setText("version " + Util.getVersionName(getActivity()));

            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            int savedCourseCount = getActivity().getSharedPreferences("saved_courses", MODE_PRIVATE).getInt("saved_course_count", 0);
            if (savedCourseCount > 0) {
                linkHistoryTextView.setVisibility(View.VISIBLE);
            } else {
                linkHistoryTextView.setVisibility(View.GONE);
            }
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
