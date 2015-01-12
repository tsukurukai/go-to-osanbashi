package tsukurukai.gotoosanbashi.models;

import android.location.Location;
import android.util.Log;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tsukurukai.gotoosanbashi.Const;
import tsukurukai.gotoosanbashi.Secret;
import tsukurukai.gotoosanbashi.Util;

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
            if ( jsonObject.has("rating") && jsonObject.has("icon")) {
                return new Spot(jsonObject.getString("name"), jsonObject.getDouble("lat"),
                        jsonObject.getDouble("lon"), jsonObject.getString("icon"),
                        jsonObject.getDouble("rating"));
            } else {
                return new Spot(jsonObject.getString("name"), jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public Spot(String name, double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public Spot(String name, double lat, double lon, String icon, double rating) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
        this.icon = icon;
        this.rating = rating;
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

    public double getRating() {
        return rating;
    }

    public String getIcon() {
        return icon;
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

    private static final int MINIMUM_SPOT_COUNT = 3;
    private static final int MINIMUM_DISTANCE = 500;
    private static final int MAXIMUM_DISTANCE = 10000;
    private static final int MAXIMUM_REQUEST_COUNT = 5;
    private static final int NUMBER_OF_SPOTS = 4;

    public static ArrayList<Spot> findByLocation(Location location) throws IOException, JSONException {
            ArrayList<Spot> spots = new ArrayList<>();

            Double currentLat = location.getLatitude();
            Double currentLng = location.getLongitude();
            Double goalLat = Const.GOAL_LATITUDE;
            Double goalLng = Const.GOAL_LONGITUDE;

            int distance = Util.betweenDistance(currentLat, currentLng, goalLat, goalLng) / 2;
            if ( distance < MINIMUM_DISTANCE ) {
                distance = MINIMUM_DISTANCE;
            } else if ( distance > MAXIMUM_DISTANCE ) {
                distance = MAXIMUM_DISTANCE;
            }
            String uri = makeSpotUri(distance);

            // 立ち寄り地の数分 request
            for (int i = 1; i < NUMBER_OF_SPOTS ; i++) {
                Pair<Double, Double> spotLatlng = Util.betweenLatLng(currentLat, currentLng, goalLat, goalLng, i, NUMBER_OF_SPOTS);
                Double spotLat = spotLatlng.first;
                Double spotLng = spotLatlng.second;

                String pageToken = ""; // pager

                int spotCount = 0;
                int requestCount = 0;
                boolean hasNext = true;
                while( spotCount < MINIMUM_SPOT_COUNT && hasNext && requestCount < MAXIMUM_REQUEST_COUNT) {
                    String spotUri = uri + "&location=" + String.valueOf(spotLat) + ","
                            + String.valueOf(spotLng) + pageToken;
                    Log.d("spotURI", "spotURI: " + spotUri);
                    HttpGet request = new HttpGet(spotUri);
                    HttpResponse httpResponse;
                    HttpClient httpClient = new DefaultHttpClient();
                    requestCount++;
                    httpResponse = httpClient.execute(request);
                    int status = httpResponse.getStatusLine().getStatusCode();
                    if (HttpStatus.SC_OK == status) {
                        JSONObject json = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
                        JSONArray results = json.getJSONArray("results");
                        if (json.has("next_page_token")) {
                            pageToken = "&pagetoken=" + json.getString("next_page_token");
                            spotCount += addSpots(spots, results);
                        } else {
                            hasNext = false;
                        }
                    } else {
                        throw new IOException("HTTP Status: " + status);
                    }
                }
            }
            return spots;
    }

    private static String makeSpotUri(int distance) {
        String baseUri  = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
        String key      = "key=" + Secret.GOOGLE_PLACES_API_KEY;
        String whiteList = Const.SPOT_TYPE_WHITE_LIST;
        String radius   = "&radius=" + String.valueOf(distance);
        String sensor   = "&sensor=false";
        String option   = "&language=" + getLocale();
        String uri = baseUri + key + radius + sensor + option;
        try {
            uri += "&types=" + URLEncoder.encode(whiteList, "utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return uri;
    }

    private static String getLocale() {
        if (Locale.JAPAN.equals(Locale.getDefault())) {
            return "ja";
        } else {
            return "en";
        }
    }

    private static int addSpots(ArrayList<Spot> spots, JSONArray results) throws JSONException {
        int spotCount = 0;
        for (int j = 0; j < results.length(); j++) {
            JSONObject result = results.getJSONObject(j);
            JSONArray typeArray = result.getJSONArray("types");
            Double lat = result.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            Double lng = result.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            String icon = result.getString("icon");
            if (!isBlackListedSpot(typeArray) && isInYokohama(lat,lng)) {
                if (result.has("rating")) {
                    Double rating = result.getDouble("rating");
                    spots.add(new Spot(result.getString("name"), lat, lng, icon, rating));
                } else {
                    spots.add(new Spot(result.getString("name"), lat, lng));
                }
                spotCount++;
            }
        }
        return spotCount;
    }

    /**
     * A method to check if a spot has a black-listed type
     * @return boolean
     */
    private static boolean isBlackListedSpot(JSONArray typeArray) throws JSONException {
        String blackList = Const.SPOT_TYPE_BLACK_LIST;
        Pattern pattern = Pattern.compile(blackList);

        for (int i = 0; i < typeArray.length(); i++) {
            String type = (String) typeArray.get(i);
            Matcher m = pattern.matcher(type);
            if ( m.find() ) {
                return true;
            }
        }
        return false;
    }

    /**
     * A method to check if a spot is in Yokohama
     * @return boolean
     */
    private static boolean isInYokohama(Double latitude, Double longitude) {
        int distance = Util.betweenDistance(latitude, longitude, Const.YOKOHAMA_CENTER_LATITUDE,
                Const.YOKOHAMA_CENTER_LONGITUDE);
        if ( distance > Const.YOKOHAMA_CENTER_RADIUS ) {
            return false;
        }
        return true;
    }

}
