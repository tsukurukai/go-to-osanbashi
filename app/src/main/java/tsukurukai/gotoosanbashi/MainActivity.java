package tsukurukai.gotoosanbashi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import tsukurukai.gotoosanbashi.activities.MapsActivity;
import tsukurukai.gotoosanbashi.models.CourseCalculator;
import tsukurukai.gotoosanbashi.models.Spot;


public class MainActivity extends ActionBarActivity {

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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            final TextView textView = (TextView)rootView.findViewById(R.id.link_map);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Spot> spots = new ArrayList<Spot>();
                    spots.add(new Spot("ランドマークタワー", 35.454721, 139.631666));
                    spots.add(new Spot("青葉台", 35.542955, 139.517182));
                    spots.add(new Spot("十日市場", 35.526302, 139.516584));
                    spots.add(new Spot("ズーラシア", 35.496483, 139.525851));

                    Spot goal = new Spot("大桟橋", 35.451762, 139.647758);
                    List<Spot> course = CourseCalculator.calculate(spots, 3, goal);
                    course.add(goal);

                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("data", getActivity().MODE_PRIVATE);
                    for (int i = 0; i < course.size(); i++) {
                        sharedPreferences
                                .edit()
                                .putString("spots:"+i, course.get(i).toJson())
                                .apply();
                    }
                    sharedPreferences
                            .edit()
                            .putInt("spotsCount", course.size())
                            .commit();

                    Intent intent = MapsActivity.createIntent(getActivity());
                    startActivity(intent);
                }
            });

            final TextView hogeView = (TextView)rootView.findViewById(R.id.hoge);
            hogeView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<Spot> spots = new ArrayList<Spot>();
                    spots.add(new Spot("大桟橋", 35.451762, 139.647758));
                    spots.add(new Spot("ランドマークタワー", 35.454721, 139.631666));
                    spots.add(new Spot("青葉台", 35.542955, 139.517182));
                    spots.add(new Spot("十日市場", 35.538908, 138.887492));
                    spots.add(new Spot("ズーラシア", 31.065960, 76.270864));

                    List<Spot> course = CourseCalculator.calculate(spots, 3, new Spot("大桟橋", 35.451762, 139.647758));
                    for (Spot s : course) {
                        Log.d("TAG", s.toJson());
                    }
                }
            });
            return rootView;
        }
    }
}
