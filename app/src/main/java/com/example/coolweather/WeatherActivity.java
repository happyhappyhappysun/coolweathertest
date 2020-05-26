package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.coolweather.gosn.Forecast;
import com.example.coolweather.gosn.LifeStyle;
import com.example.coolweather.gosn.Weather;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.zip.Inflater;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private ScrollView weatherLayout;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView humText;
    private TextView windText;
    private TextView comfortText;
    private TextView drsgText;
    private TextView sportText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        init();
        Weather weather = new Weather();
        //缓存中分别储存的。
        SharedPreferences  prefs = MyApplication.getContext().getSharedPreferences("weather_data", MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.clear();
//        editor.commit();
        String nowString = prefs.getString("now",null);
        String forecastString = prefs.getString("forecast",null);
        String lifestyleString = prefs.getString("lifestyle", null);
        if(TextUtils.isEmpty(nowString)||TextUtils.isEmpty(forecastString)||TextUtils.isEmpty(lifestyleString)){
            //缓存中没有数据的时候联网查询（首先是确定查询的是哪个城市的weather）
            String weatherId = getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId, weather, "now");
        }else{
            // 有缓存时直接读取数据(缓存中放的就是json字符串)
            weather = Utility.handleWeatherResponse(nowString,weather,"now");
            weather = Utility.handleWeatherResponse(forecastString, weather, "forecast");
            weather = Utility.handleWeatherResponse(lifestyleString, weather, "lifestyle");
            assert weather != null;//判断语句
            showWeatherInfo(weather);
        }
    }

    /*
    联网查询天气情况
     */
    private void requestWeather(final String weatherId, final Weather weather, final String cate) {
        String requestUrl = reUrl(cate, weatherId);
        Log.d("UUU", requestUrl);
        HttpUtil.sendOkHttpRequest(requestUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (cate) {
                            case "now":
                                Toast.makeText(WeatherActivity.this, "获取今天天气失败", Toast.LENGTH_SHORT).show();
                                break;
                            case "lifestyle":
                                Toast.makeText(WeatherActivity.this, "获取生活指数失败", Toast.LENGTH_SHORT).show();
                                break;
                            case "forecast":
                                Toast.makeText(WeatherActivity.this, "获取预报天气失败", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                });
            }

            @Override
            public void onResponse(@NotNull final Call call, @NotNull Response response) throws IOException {
                final String responseText = response.body().string();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((weather != null)){
                            //这里存储的是now，就是当前的天气情况，没有其他的建议什么的，后面可以依次传入不同的cate来实现网络查询
                            SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("weather_data", MODE_PRIVATE).edit();
                            Weather weather_new = Utility.handleWeatherResponse(responseText, weather, cate);
                            editor.putString(cate, responseText);
                            editor.apply();
                            switch (cate) {
                                case "now":
                                    requestWeather(weatherId, weather_new, "forecast");
                                    break;
                                case "forecast":
                                    requestWeather(weatherId, weather_new, "lifestyle");
                                    break;
                                case "lifestyle":
                                    assert weather_new != null;
                                    showWeatherInfo(weather_new);
                                    break;
                            }
                        }else {
                            Toast.makeText(WeatherActivity.this, "请求到的数据出错", Toast.LENGTH_SHORT).show();
                        }

                    }
                });
            }
        });
    }

    private String reUrl(String cate, String weatherId) {
        String key = "93ed3bc8991a4841bdf9d1122fc46bfc";
        return "https://free-api.heweather.net/s6/weather/" + cate + "?location=" + weatherId + "&key=" + key;
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.update.updateTime;
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.info;
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime.split(" ")[1]);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        //手动添加预测信息，如果是首次联网查询则先让其消失，要不然看这不好看
        forecastLayout.removeAllViews();
        for(Forecast forecast:weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = view.findViewById(R.id.date_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            //根据早晚的天气情况显示
            if (forecast.day.equals(forecast.night)){
                infoText.setText(forecast.day);
            }else {
                infoText.setText(String.format("%s转%s", forecast.day, forecast.night));
            }
            maxText.setText(forecast.tmp_max);
            minText.setText(forecast.tmp_min);
            //一定要记得添加进布局里面
            forecastLayout.addView(view);
        }
        if(weather.now != null){
            humText.setText(weather.now.humidity);
            windText.setText(weather.now.directionOfWind);
        }
        for(LifeStyle lifestyle: weather.lifeStyleList){
            if ("comf".equals(lifestyle.type)){
                comfortText.setText(String.format("舒适度：%s", lifestyle.txt));
            }
            if ("drsg".equals(lifestyle.type)){
                drsgText.setText(String.format("穿衣指数：%s", lifestyle.txt));
            }
            if ("sport".equals(lifestyle.type)){
                sportText.setText(String.format("运动建议：%s", lifestyle.txt));
            }
        }
        //最后记得将布局显示出来
        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void init() {
        // 初始化控件
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        weatherInfoText = findViewById(R.id.weather_info_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        humText = findViewById(R.id.hum_text);
        windText = findViewById(R.id.wind_text);
        comfortText = findViewById(R.id.comfort_text);
        drsgText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        degreeText = findViewById(R.id.degree_text);
    }
}
