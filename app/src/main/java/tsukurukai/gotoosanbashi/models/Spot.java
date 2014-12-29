package tsukurukai.gotoosanbashi.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Spot implements Parcelable {

    private String name;
    private double lat;
    private double lon;

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
