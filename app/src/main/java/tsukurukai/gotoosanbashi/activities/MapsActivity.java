package tsukurukai.gotoosanbashi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.MapConfigurer;
import tsukurukai.gotoosanbashi.R;
import tsukurukai.gotoosanbashi.models.Spot;

public class MapsActivity extends ActionBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<Spot> spots;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addCourse(0);

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Button button1 = (Button)findViewById(R.id.course_1);
        final Button button2 = (Button)findViewById(R.id.course_2);
        final Button button3 = (Button)findViewById(R.id.course_3);
        final Button courseSave = (Button)findViewById(R.id.course_save);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCourse(0);
                setUpMapIfNeeded();

                selectedButton(button1);
                unSelectedButton(button2);
                unSelectedButton(button3);
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCourse(1);
                setUpMapIfNeeded();

                selectedButton(button2);
                unSelectedButton(button1);
                unSelectedButton(button3);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCourse(2);
                setUpMapIfNeeded();

                selectedButton(button3);
                unSelectedButton(button1);
                unSelectedButton(button2);
            }
        });

        courseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);

                int selectedCourse = sharedPreferences.getInt("selected_course", 0);
                int spotsCount = sharedPreferences.getInt("course:" + selectedCourse + ":spotsCount", 0);

                SharedPreferences saveCourseSharedPreferences = getSharedPreferences("saved_courses", MODE_PRIVATE);
                int savedCourseCount = saveCourseSharedPreferences.getInt("saved_course_count", 0);

                for (int i = 0; i < spotsCount; i++) {
                    String spotJson = sharedPreferences.getString("course:" + selectedCourse + ":spots:" + i, "");
                    saveCourseSharedPreferences
                            .edit()
                            .putString("saved_course:" + savedCourseCount + ":spots:" + i, spotJson)
                            .apply();
                }
                saveCourseSharedPreferences
                        .edit()
                        .putLong("saved_course_time:" + savedCourseCount, System.currentTimeMillis())
                        .apply();

                saveCourseSharedPreferences
                        .edit()
                        .putInt("saved_course_count", ++savedCourseCount)
                        .commit();


                Intent intent = SavedMapActivity.createIntent(getApplicationContext(), --savedCourseCount);
                startActivity(intent);
                finish();
            }
        });
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setUpMap() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnInfoWindowClickListener(MapConfigurer.getOnInfoWindowClickListener(this));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.showInfoWindow();
                return true;
            }
        });
    }

    private void addCourse(int courseId) {
        spots = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int spotsCount = sharedPreferences.getInt("course:" + courseId + ":spotsCount", 0);
        for (int i = 0; i < spotsCount; i++) {
            spots.add(Spot.fromJson(sharedPreferences.getString("course:" + courseId + ":spots:" + i, "")));
        }

        sharedPreferences
                .edit()
                .putInt("selected_course", courseId)
                .commit();
    }

    private void selectedButton(Button button) {
        button.setBackground(getResources().getDrawable(R.drawable.course_button_selected));
        button.setTextColor(getResources().getColor(R.color.white));
    }

    private void unSelectedButton(Button button) {
        button.setBackground(getResources().getDrawable(R.drawable.course_button));
        button.setTextColor(getResources().getColor(R.color.text_color_black_87));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        MapConfigurer.addMarker(mMap, spots, getResources());
        MapConfigurer.addPolyLineOptions(mMap, spots);
        MapConfigurer.moveCamera(mMap, spots);
    }

}
