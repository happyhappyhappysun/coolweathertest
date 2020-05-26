package com.example.coolweather.util;

import android.text.TextUtils;

import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.example.coolweather.gosn.Forecast;
import com.example.coolweather.gosn.Now;
import com.example.coolweather.gosn.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.locks.ReentrantLock;

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
    // 将返回的数据解析成实体类,注意哦：天气由于不同的请求参数就会有不同的结果。所以这里采用分情况而定,打断点看一下是什么数据

    /**
     * 这里行不通的话就自己写一个接口，类似郭霖那个，然后获取到所有的数据，就是多调几次网路请求.？？？？？？？？？？？？？
     */
    public static Weather handleWeatherResponse(String response, Weather weather, String cate){
            try {
                JSONObject object = new JSONObject(response);
                JSONArray jsonArray = object.getJSONArray("HeWeather6");
                String weatherContent = jsonArray.getJSONObject(0).toString();
                if("now".equals(cate)){
                    Weather nowWeather = new Gson().fromJson(weatherContent,Weather.class);
                    weather.basic = nowWeather.basic;
                    weather.update = nowWeather.update;
                    weather.now = nowWeather.now;
                }else if("forecast".equals(cate)){
                    Weather forecastWeather = new Gson().fromJson(weatherContent, Weather.class);
                    weather.forecastList = forecastWeather.forecastList;
                    //weather.status = forecastWeather.status;
                }else if("lifestyle".equals(cate)){
                    Weather lifeStyleWeather = new Gson().fromJson(weatherContent, Weather.class);
                    weather.lifeStyleList = lifeStyleWeather.lifeStyleList;
                    //weather.status = lifeStyleWeather.status;
                }
                return weather;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return null;
    }
}

