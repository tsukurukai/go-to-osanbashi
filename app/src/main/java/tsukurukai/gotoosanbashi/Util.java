package tsukurukai.gotoosanbashi;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Pair;

import java.util.Random;

public class Util {
    public static Pair<Double, Double> betweenLatLng(double startLat, double startLng, double goalLat, double goalLng, int index, int allCount) {
        Double spotLat = ( startLat * index / allCount ) + ( goalLat * (allCount - index) / allCount );
        Double spotLng = ( startLng * index / allCount ) + ( goalLng * (allCount - index) / allCount );
        return Pair.create(spotLat, spotLng);
    }

    public static int betweenDistance(double startLat, double startLng, double goalLat, double goalLng) {
        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, goalLat, goalLng, results);
        if ( results != null && results.length > 0 ) {
            return (int) results[0];
        }
        return 0;
    }
    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();
        String versionName = "";
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            throw new IllegalStateException((e));
        }
        return versionName;
    }

    public static Location getRandomLocation(Location location) {
        Double latitude = location.getLatitude();
        Double longitude = location.getLongitude();

        Random random = new Random();

        Double randomLatitude = random.nextDouble() * 0.187;
        Double randomLongitude = random.nextDouble() * 0.240;

        if (random.nextBoolean()) {
            latitude += randomLatitude;
        } else {
            latitude -= randomLatitude;
        }

        if (random.nextBoolean()) {
            longitude += randomLongitude;
        } else {
            longitude -= randomLongitude;
        }

        Location randomLocation = new Location("random");
        randomLocation.setLatitude(latitude);
        randomLocation.setLongitude(longitude);

        return randomLocation;
    }

}
