package com.example.coolweather;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class MyApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //由于Litepal也需要配置application name，但是一个应用只能有一个application，所以在这里初始化litepal就可以了
        LitePal.initialize(context);
    }
    public static Context getContext(){
        return context;
    }

}
