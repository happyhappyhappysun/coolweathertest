package com.example.coolweather.gosn;

import com.google.gson.annotations.SerializedName;

public class Basic {
    @SerializedName("location")
    public String cityName;
    @SerializedName("cid")
    public String weatherId;

}
