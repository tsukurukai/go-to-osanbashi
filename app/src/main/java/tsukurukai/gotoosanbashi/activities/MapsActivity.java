package tsukurukai.gotoosanbashi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tsukurukai.gotoosanbashi.R;
import tsukurukai.gotoosanbashi.models.Spot;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<Spot> spots;

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, MapsActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        spots = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        int spotsCount = sharedPreferences.getInt("course:0:spotsCount", 0);
        for (int i = 0; i < spotsCount; i++) {
            spots.add(Spot.fromJson(sharedPreferences.getString("course:0:spots:" + i, "")));
        }

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        Button button1 = (Button)findViewById(R.id.course_1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spots = new ArrayList<>();
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                int spotsCount = sharedPreferences.getInt("course:0:spotsCount", 0);
                for (int i = 0; i < spotsCount; i++) {
                    spots.add(Spot.fromJson(sharedPreferences.getString("course:0:spots:" + i, "")));
                }
                setUpMapIfNeeded();
            }
        });


        Button button2 = (Button)findViewById(R.id.course_2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spots = new ArrayList<>();
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                int spotsCount = sharedPreferences.getInt("course:1:spotsCount", 0);
                for (int i = 0; i < spotsCount; i++) {
                    spots.add(Spot.fromJson(sharedPreferences.getString("course:1:spots:" + i, "")));
                }
                setUpMapIfNeeded();
            }
        });


        Button button3 = (Button)findViewById(R.id.course_3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spots = new ArrayList<>();
                SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                int spotsCount = sharedPreferences.getInt("course:2:spotsCount", 0);
                for (int i = 0; i < spotsCount; i++) {
                    spots.add(Spot.fromJson(sharedPreferences.getString("course:2:spots:" + i, "")));
                }
                setUpMapIfNeeded();
            }
        });
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.469561, 139.599325), 10));
        for (Spot spot: spots) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(spot.getLat(), spot.getLon())).title(spot.getName()));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        PolylineOptions polyLine = new PolylineOptions();

        for (Spot spot: spots) {
            polyLine.geodesic(true).add(new LatLng(spot.getLat(), spot.getLon()));
        }

        googleMap.clear();
        for (Spot spot: spots) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(spot.getLat(), spot.getLon())).title(spot.getName()));
        }
        googleMap.addPolyline(polyLine);
    }
}
