package tsukurukai.gotoosanbashi.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import tsukurukai.gotoosanbashi.R;

public class TopPageBackground {
    public static Drawable getImageByTime(Context context, Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int drawableId;
        if (5 <= hour && hour < 16) {
            int i = new Random(d.getTime()).nextInt(100);
            if (5 <= i && i < 10) drawableId = R.drawable.osanbashi_top_noon_rare1;
            else if (10 <= i)     drawableId = R.drawable.osanbashi_top_noon;
            else                  drawableId = R.drawable.osanbashi_top_noon_rare2;
        } else if (16 <= hour && hour < 18) {
            drawableId = R.drawable.osanbashi_top_evening;
        } else if (18 <= hour && hour < 19) {
            int i = new Random(d.getTime()).nextInt(100);
            if (5 <= i) drawableId = R.drawable.osanbashi_top_twilite;
            else        drawableId = R.drawable.osanbashi_top_twilite_rare;
        } else {
            drawableId = R.drawable.osanbashi_top_night;
        }
        return context.getResources().getDrawable(drawableId);
    }
}
