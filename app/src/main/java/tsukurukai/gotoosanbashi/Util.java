package tsukurukai.gotoosanbashi;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Pair;

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
}
