package tsukurukai.gotoosanbashi.models;

import android.location.Location;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CourseCalculator {
    public static List<Spot> calculate(List<Spot> spots, int n, Spot start, Spot goal) {
        if (spots.size() < n) throw new IllegalArgumentException("spots size is less than n. [size=" + spots.size() + ", n=" + n);

        List<Pair<Integer, Spot>> selectedSpot = new ArrayList<>(n);
        selectedSpot.add(Pair.create(Integer.MAX_VALUE, start));

        List<Spot> copyList = new ArrayList<>(spots);
        for (int i = 0; i < n; i++) {
            int j = (int) (Math.random() * (spots.size() - i));
            Spot s = copyList.get(j);
            selectedSpot.add(Pair.create(getDistance(s, goal), s));
            copyList.set(j, copyList.get(spots.size() - i - 1));
        }
        selectedSpot.add(Pair.create(0, goal));

        Collections.sort(selectedSpot, new Comparator<Pair<Integer, Spot>>() {
            @Override
            public int compare(Pair<Integer, Spot> lhs, Pair<Integer, Spot> rhs) {
                return lhs.first - rhs.first;
            }
        });
        List<Spot> results = new ArrayList<>(selectedSpot.size());
        for (Pair<Integer, Spot> pair : selectedSpot) {
            results.add(pair.second);
        }
        Collections.reverse(results);
        return results;
    }

    private static int getDistance(Spot a, Spot b) {
        float[] results = new float[1];
        Location.distanceBetween(a.getLat(), a.getLon(), b.getLat(), b.getLon(), results);
        return (int)results[0];
    }
}
