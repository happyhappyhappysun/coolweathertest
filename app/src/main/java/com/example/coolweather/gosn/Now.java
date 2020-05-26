package com.example.coolweather.gosn;

import com.google.gson.annotations.SerializedName;

public class Now {
    @SerializedName("tmp")
    public String temperature;

    @SerializedName("cond_txt")
    public String info;

    @SerializedName("wind_dir")
    public String directionOfWind;

    @SerializedName("wind_sc")
    public String powerOfWind;

    @SerializedName("hum")
    public String humidity;

    @SerializedName("vis")
    public String visibility;

}
