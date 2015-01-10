package tsukurukai.gotoosanbashi.activities;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.R;
import tsukurukai.gotoosanbashi.models.Spot;

/**
 * Created by trtraki on 2015/01/10.
 */
public class HistoryListActivity extends FragmentActivity {

    public static Intent createIntent(Context context) {
        Intent intent = new Intent(context, HistoryListActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_list);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CourseHistoryFragment())
                    .commit();
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class CourseHistoryFragment extends ListFragment {

        public CourseHistoryFragment() {

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            List<CourseData> courseDatas = new ArrayList<>();

            SharedPreferences saveCourseSharedPreferences = getActivity().getSharedPreferences("saved_courses", MODE_PRIVATE);
            int savedCourseCount = saveCourseSharedPreferences.getInt("saved_course_count", 0);

            for (int i = 0; i < savedCourseCount; i++) {
                List<Spot> spots = new ArrayList<>();
                for (int j = 0; j < 5; j++) {
                    String spotJson = saveCourseSharedPreferences.getString("saved_course:" + i + ":spots:" + j, "");
                    System.out.println(spotJson);
                    Spot spot = Spot.fromJson(spotJson);
                    spots.add(spot);
                }
                CourseData course = new CourseData();
                course.setSpots(spots);
                courseDatas.add(course);
            }
            CourseHistoryAdapter adapter = new CourseHistoryAdapter(getActivity(), android.R.layout.simple_list_item_1, courseDatas);

            setListAdapter(adapter);
        }

    }

    public static class CourseHistoryAdapter extends ArrayAdapter<CourseData> {
        private List<CourseData> courseDataList;

        public CourseHistoryAdapter(Context context, int resource, List<CourseData> courseDataList) {
            super(context, resource);
            this.courseDataList = courseDataList;
        }


        @Override
        public int getCount() {
            return courseDataList.size();
        }

        @Override
        public CourseData getItem(int position) {
            return courseDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return courseDataList.get(position).getOrder();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listitem_courses, null);
                TextView orderView = (TextView)convertView.findViewById(R.id.course_order);
                orderView.setText(Integer.toString(position + 1));

                String title = new String();
                TextView courseTitle = (TextView)convertView.findViewById(R.id.course_title);
                for (int i = 1; i < 5; i++) {
                    if (i > 1) {
                        title += "　→　︎";
                    }
                    Spot spot = courseDataList.get(position).getSpots().get(i);
                    title += spot.getName();
                }

                courseTitle.setText(title);
            }
            return convertView;
        }
    }

    private static class CourseData {
        List<Spot> spots;
        int order;

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public List<Spot> getSpots() {
            return spots;
        }

        public void setSpots(List<Spot> spots) {
            this.spots = spots;
        }
    }
}
