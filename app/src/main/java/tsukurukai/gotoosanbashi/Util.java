package tsukurukai.gotoosanbashi;

import android.util.Pair;

public class Util {
    public static Pair<Double, Double> betweenLatLng(double startLat, double startLng, double goalLat, double goalLng, int index, int allCount) {
        Double spotLat = ( startLat * index / allCount ) + ( goalLat * (allCount - index) / allCount );
        Double spotLng = ( startLng * index / allCount ) + ( goalLng * (allCount - index) / allCount );
        return Pair.create(spotLat, spotLng);
    }

}
