package tsukurukai.gotoosanbashi.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class Spot implements Parcelable {

    private String name;
    private double lat;
    private double lon;

    public static Spot fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            return new Spot(
                    jsonObject.getString("name"),
                    jsonObject.getDouble("lat"),
                    jsonObject.getDouble("lon"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Spot(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(lat);
        dest.writeDouble(lon);
    }

    public static final Creator<Spot> CREATOR = new Creator<Spot>() {
        @Override
        public Spot createFromParcel(Parcel source) {
            return new Spot(source.readString(), source.readDouble(), source.readDouble());
        }

        @Override
        public Spot[] newArray(int size) {
            return new Spot[size];
        }
    };
}
