package tsukurukai.gotoosanbashi;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Pair;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import tsukurukai.gotoosanbashi.models.Spot;

public class MapConfigurer {

    public static GoogleMap.OnInfoWindowClickListener getOnInfoWindowClickListener(final Activity activity) {
        return new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Uri uri = Uri.parse("https://www.google.co.jp/#q=" + marker.getTitle());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                activity.startActivity(intent);
            }
        };
    }

    public static void addMarker(GoogleMap map, List<Spot> spots) {
        for (Spot spot: spots) {
            map.addMarker(new MarkerOptions().position(new LatLng(spot.getLat(), spot.getLon())).title(spot.getName()));
        }
    }

    public static void addPolyLineOptions(GoogleMap map, List<Spot> spots) {
        PolylineOptions polylineOptions = new PolylineOptions();
        for (Spot spot: spots) {
            polylineOptions.geodesic(true).add(new LatLng(spot.getLat(), spot.getLon()));
        }
        map.addPolyline(polylineOptions);
    }

    public static void moveCamera(GoogleMap map, List<Spot> spots) {
        Spot startSpot = spots.get(1);
        Spot goal = spots.get(spots.size()-1);
        Pair<Double, Double> center = Util.betweenLatLng(startSpot.getLat(), startSpot.getLon(), goal.getLat(), goal.getLon(), 1, 2);
        float[] results = new float[1];
        Location.distanceBetween(startSpot.getLat(), startSpot.getLon(), goal.getLat(), goal.getLon(), results);
        float distance = results[0];
        CameraUpdate cameraUpdate;
        if (distance < 2000) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 14);
        } else if (distance < 3000) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 13);
        } else if (distance < 4000) {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 12);
        } else {
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(center.first, center.second), 10);
        }
        map.moveCamera(cameraUpdate);
    }
}
