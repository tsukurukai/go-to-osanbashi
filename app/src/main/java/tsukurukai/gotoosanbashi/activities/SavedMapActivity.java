package tsukurukai.gotoosanbashi.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.R;
import tsukurukai.gotoosanbashi.Util;
import tsukurukai.gotoosanbashi.models.Spot;

public class SavedMapActivity extends ActionBarActivity implements OnMapReadyCallback {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private List<Spot> spots;

    public static Intent createIntent(Context context, int order) {
        Intent intent = new Intent(context, SavedMapActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        int order = intent.getIntExtra("order", 0);

        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("saved_courses", MODE_PRIVATE);
        spots = new ArrayList<>();
        for (int j = 0; j < 5; j++) {
            String spotJson = sharedPreferences.getString("saved_course:" + order + ":spots:" + j, "");
            Spot spot = Spot.fromJson(spotJson);
            spots.add(spot);
        }

        setContentView(R.layout.activity_saved_map);
        setUpMapIfNeeded();

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
     * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(35.469561, 139.599325), 10));
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Uri uri = Uri.parse("https://www.google.co.jp/#q=" + marker.getTitle());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    private void addMarker() {
        for (Spot spot: spots) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(spot.getLat(), spot.getLon())).title(spot.getName()));
        }
    }

    private void addPolyLine(GoogleMap googleMap) {
        PolylineOptions polyLine = new PolylineOptions();
        for (Spot spot: spots) {
            polyLine.geodesic(true).add(new LatLng(spot.getLat(), spot.getLon()));
        }
        googleMap.addPolyline(polyLine);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.clear();
        addMarker();
        addPolyLine(googleMap);
        setCameraPosition();
    }

    private void setCameraPosition() {
        Spot startSpot = spots.get(1);
        Spot goal = spots.get(spots.size()-1);
        Pair<Double, Double> center = Util.betweenLatLng(startSpot.getLat(), startSpot.getLon(), goal.getLat(), goal.getLon(), 1, 2);
        float[] results = new float[1];
        Location.distanceBetween(startSpot.getLat(), startSpot.getLon(), goal.getLat(), goal.getLon(), results);
        float distance = results[0];
        if (distance < 4000) mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 12));
        else mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 10));
    }
}
