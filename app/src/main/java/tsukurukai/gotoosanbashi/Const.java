package tsukurukai.gotoosanbashi;

/**
 * Created by satot on 2014/12/29.
 */
public class Const {
    public static final String GOAL_NAME = "大さん橋";
    public static final Double GOAL_LATITUDE  = 35.451762;
    public static final Double GOAL_LONGITUDE = 139.647758;
    public static final Double YOKOHAMA_CENTER_LATITUDE = 35.450762; // 横浜市桜台小学校
    public static final Double YOKOHAMA_CENTER_LONGITUDE = 139.590334;
    public static final int YOKOHAMA_CENTER_RADIUS = 15000; // r = 15km
    public static final String SPOT_TYPE_WHITE_LIST = "amusement_park|aquarium|art_gallery" +
            "|bowling_alley|church|hindu_temple|library|mosque|museum|park|place_of_worship" +
            "|shopping_mall|spa|synagogue|university|zoo";
    public static final String SPOT_TYPE_BLACK_LIST = "bakery|cafe|convenience_store|food" +
            "|restaurant|school";
}
