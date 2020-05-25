package com.example.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 发送网路请求，请求数据
 */
public class HttpUtil {
    //这里是无返回值的原因在于接口回调传递参数，OKhttp3里面自带接口
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }

}
