package tsukurukai.gotoosanbashi.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import tsukurukai.gotoosanbashi.MainActivity;
import tsukurukai.gotoosanbashi.models.CourseCalculator;

/**
 * Created by trtraki on 2014/12/29.
 */
public class CoursePagerAdapter extends FragmentPagerAdapter {

    public CoursePagerAdapter(FragmentManager fm) {
        super(fm);
        //this.myCourse = new ArrayList<>();
    }

    @Override
    public Fragment getItem(int i) {
        /*
        switch(i){
            case 0:
                return new Fragment1();
            case 1:
                return new Fragment2();
            default:
                return new ItemListFragment();
        }
        */
        return null;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object o) {
        return false;
    }
}
