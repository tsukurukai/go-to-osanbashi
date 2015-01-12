package tsukurukai.gotoosanbashi.models;

import android.location.Location;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CourseCalculator {
    public static List<Spot> calculate(List<Spot> spots, int n, Spot start, Spot goal) {
        if (spots.size() < n) throw new IllegalArgumentException("spots size should be greater than n. [size=" + spots.size() + ", n=" + n);

        List<Pair<Integer, Spot>> selectedSpot = selectSpots(spots, n, start, goal);

        sort(selectedSpot);

        List<Spot> results = new ArrayList<>(selectedSpot.size());
        for (Pair<Integer, Spot> pair : selectedSpot) {
            results.add(pair.second);
        }
        Collections.reverse(results);
        return results;
    }

    /**
     * @return ゴールからの距離とSpotのPairオブジェクト
     */
    private static List<Pair<Integer, Spot>> selectSpots(List<Spot> spots, int n, Spot start, Spot goal) {
        List<Pair<Integer, Spot>> results = new ArrayList<>(n);
        results.add(Pair.create(Integer.MAX_VALUE, start));

        List<Spot> copySpots = new ArrayList<>(spots);
        for (int i = 0; i < n; i++) {
            int j = (int) (Math.random() * (spots.size() - i));
            Spot selected = copySpots.get(j);
            results.add(Pair.create(getDistance(selected, goal), selected));
            copySpots.set(j, copySpots.get(spots.size() - i - 1));
        }

        results.add(Pair.create(0, goal));
        return results;
    }

    private static void sort(List<Pair<Integer, Spot>> selectedSpot) {
        Collections.sort(selectedSpot, new Comparator<Pair<Integer, Spot>>() {
            @Override
            public int compare(Pair<Integer, Spot> lhs, Pair<Integer, Spot> rhs) {
                return lhs.first - rhs.first;
            }
        });
    }

    private static int getDistance(Spot a, Spot b) {
        float[] results = new float[1];
        Location.distanceBetween(a.getLat(), a.getLon(), b.getLat(), b.getLon(), results);
        return (int)results[0];
    }
}
