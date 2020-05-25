package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 解析json数据，网络请求返回的是json形式的数据
 * 保存到数据库中
 */
public class Utility {

    public static boolean handleProvinceResponse(String responseText) {
        if(!TextUtils.isEmpty(responseText)){
            try {
                JSONArray allProvinces = new JSONArray(responseText);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String responseText, int id) {
        if(!TextUtils.isEmpty(responseText)){
            try {
                JSONArray allCities = new JSONArray(responseText);
                for(int i=0;i<allCities.length();i++){
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityCode(cityObject.getInt("id"));
                    city.setCityName(cityObject.getString("name"));
                    city.setProvinceId(id);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String responseText, int id) {
        if(!TextUtils.isEmpty(responseText)){
            try {
                JSONArray allCounties = new JSONArray(responseText);
                for(int i=0;i<allCounties.length();i++){
                    JSONObject countryObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countryObject.getString("name"));
                    county.setWeatherId(countryObject.getString("weather_id"));
                    county.setCityId(id);
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

