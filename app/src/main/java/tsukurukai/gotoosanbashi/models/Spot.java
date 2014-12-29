package tsukurukai.gotoosanbashi.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Spot {

    private String formattedAddress;
    private double lat;
    private double lon;
    private String icon;
    private String id;
    private String name;
    private double rating;
    private String reference;
    private String[] types;

    public static Spot fromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
//            JSONArray jsonArray = jsonObject.getJSONArray("types");
//            String[] types = new String[jsonArray.length()];
//            for (int i = 0; i < jsonArray.length(); i++) {
//                types[i] = jsonArray.getString(i);
//            }
//            return new Spot(
//                    jsonObject.getString("formattedAddress")
//                    ,jsonObject.getDouble("lat")
//                    ,jsonObject.getDouble("lon")
//                    ,jsonObject.getString("icon")
//                    ,jsonObject.getString("id")
//                    ,jsonObject.getString("name")
//                    ,jsonObject.getDouble("rating")
//                    ,jsonObject.getString("reference")
//                    ,types
//            );
            return new Spot(jsonObject.getString("name"), jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Spot(String name, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public Spot(String formattedAddress, double lat, double lon, String icon, String id,
                String name, double rating, String reference, String[] types) {
        this.formattedAddress = formattedAddress;
        this.lat = lat;
        this.lon = lon;
        this.icon = icon;
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.reference = reference;
        this.types = types;
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
            jsonObject.put("formattedAddress", formattedAddress);
            jsonObject.put("lat", lat);
            jsonObject.put("lon", lon);
            jsonObject.put("icon", icon);
            jsonObject.put("id", id);
            jsonObject.put("name", name);
            jsonObject.put("rating", rating);
            jsonObject.put("reference", reference);
            jsonObject.put("types", types);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return jsonObject.toString();
    }
}
