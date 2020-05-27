package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.example.coolweather.gosn.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.Savepoint;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 2 * 60 * 60 * 1000;    // 每两小时更新一次
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent i = new Intent(this,AutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this,0,i,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        //联网查询图片，放进缓存中
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = getSharedPreferences("weather_data", MODE_PRIVATE).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }

    private void updateWeather() {
        //联网查询天气并且放进缓存中
        SharedPreferences prefs = getSharedPreferences("weather_data",MODE_PRIVATE);
        String nowString = prefs.getString("now",null);
        //如果缓存中没有数据，说明第一次，不用去后台更新
        if(nowString!=null){
            String weatherId = Utility.handleWeatherResponse(nowString,new Weather(),"now").basic.weatherId;
            SaveData(reUrl("now",weatherId),"now");
            SaveData(reUrl("forecast",weatherId),"forecast");
            SaveData(reUrl("lifestyle",weatherId),"lifestyle");
        }
    }

    private void SaveData(String address, final String cate) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                SharedPreferences.Editor editor = getSharedPreferences("weather_data",MODE_PRIVATE).edit();
                editor.putString(cate,responseText);
                editor.apply();
            }
        });
    }

    private String reUrl(String cate, String weatherId) {
        String key = "93ed3bc8991a4841bdf9d1122fc46bfc";
        return "https://free-api.heweather.net/s6/weather/" + cate + "?location=" + weatherId + "&key=" + key;
    }
}
