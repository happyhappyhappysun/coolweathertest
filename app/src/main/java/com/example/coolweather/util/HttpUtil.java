package com.example.coolweather.util;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 发送网路请求，请求数据
 */
public class HttpUtil {
    //这里是无返回值的原因在于接口回调传递参数，OKhttp3里面自带接口
    public static void sendOkHttpRequest(final String address, final okhttp3.Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(address).build();
                    client.newCall(request).enqueue(callback);
//                    Response response = client.newCall(request).execute();
//                    String data = response.body().string();
//                    Log.d("etxt",data);
            }
        }).start();

        //client.newCall(request).enqueue(callback);

    }

}
