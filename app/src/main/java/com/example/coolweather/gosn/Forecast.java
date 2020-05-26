package com.example.coolweather.gosn;

import com.google.gson.annotations.SerializedName;

public class Forecast {
    public String date;

    @SerializedName("sr")
    public String sunrise;

    @SerializedName("ss")
    public String sunset;

    public String tmp_max;
    public String tmp_min;

    @SerializedName("cond_txt_d")
    public String day;

    @SerializedName("cond_txt_n")
    public String night;

    @SerializedName("pop")
    public String precipitationProbability;

}
