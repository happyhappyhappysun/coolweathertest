package com.example.coolweather.gosn;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Weather {
    public Basic basic;
    public Update update;
    public Now now;
    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
    @SerializedName("lifestyle")
    public List<LifeStyle> lifeStyleList;
}
