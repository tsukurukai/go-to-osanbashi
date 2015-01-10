package tsukurukai.gotoosanbashi.activities;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.R;

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
            List<String> courseData = new ArrayList<String>();

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, courseData);

            setListAdapter(arrayAdapter);
        }

    }
}
