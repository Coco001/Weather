package cqupt.weather.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cqupt.weather.db.Myopenhelper;
import cqupt.weather.gson.Weather;

public class Utility {
    private static Myopenhelper oh;
    private static ContentValues cv = new ContentValues();
    public static boolean handleProvinceResponse(String response, Context context) {
        if (!TextUtils.isEmpty(response)) {
            oh = new Myopenhelper(context);
            SQLiteDatabase database = oh.getWritableDatabase();
            try {
                JSONArray array = new JSONArray(response);
                for (int i = 0; i < array.length(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    cv.clear();
                    cv.put("code", object.getInt("id"));
                    cv.put("name", object.getString("name"));
                    database.insert("Province", null, cv);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, int provinceId, Context context) {
        if (!TextUtils.isEmpty(response)) {
            oh = new Myopenhelper(context);
            SQLiteDatabase database = oh.getWritableDatabase();
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    cv.clear();
                    cv.put("code", cityObject.getInt("id"));
                    cv.put("name", cityObject.getString("name"));
                    cv.put("provinceId", provinceId);
                    database.insert("City", null, cv);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, int cityId, Context context) {
        if (!TextUtils.isEmpty(response)) {
            oh = new Myopenhelper(context);
            SQLiteDatabase database = oh.getWritableDatabase();
            try {
                JSONArray allCounties = new JSONArray(response);
                for (int i = 0; i < allCounties.length(); i++) {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    cv.clear();
                    cv.put("code", countyObject.getInt("id"));
                    cv.put("name", countyObject.getString("name"));
                    cv.put("cityId", cityId);
                    cv.put("weatherId",countyObject.getString("weather_id"));
                    database.insert("County", null, cv);
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static Weather handleWeatherResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent, Weather.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
