# coolweathertest
This is a testproject!
酷欧天气开发
1. 添加依赖：注意androidx的迁移，查表
全局获取context方法，以及和litepal的结合使用 
2. 创建数据库和表，这里采用的是Litepal数据库
首先需要实体类，由于Litepal采取的是映射方式，所以首先需要实体类
然后采用配置文件将实体类和表映射起来。
3.网络请求（OKHttp）
//这里是无返回值的原因在于接口回调传递参数，OKhttp3里面自带接口

public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder().url(address).build();
    client.newCall(request).enqueue(callback);
}

4. 需要一个碎片，由于获取城市相关信息会在很多地方重用，所以这里采用碎片而不是活动形式。
更改style.xml中代码，修改主题，采用无actionbar的格式。
style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar"
在fragment中自定义标题栏，由于会更新标题栏的text，显示城市信息还有返回按钮，最好不要在碎片里面使用toolbar。
Litepal中查询时DataSupport已经弃用了，采用LitepalSupport类。注意哦：要让所有的实体类都extends LitepalSupport，然后才可以完成映射数据表，直接操作。
provinceList = LitePal.findAll(Province.class);
注意：在这里，字段是provinceid，但是在实体类中的属性是provinceID。
cityList = LitePal.where("provinceid = ?",String.valueOf(selectedProvince.getId())).find(City.class);
网络请求是在子线程进行的，OKhttp封装好的，但是UI更新需要回到主线程。

HttpUtil.sendOkHttpRequest(address, new Callback() {
    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {
    }
    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        String responseText = response.body().string();
        boolean result = false;//这里是标志联网查询的结果，如果请求成功那么就可以更新UI
        if("province".equals(type)){
            result = Utility.handleProvinceResponse(responseText);
        } else if ("city".equals(type)) {
            result = Utility.handleCityResponse(responseText);
        }else if("country".equals(type)){
            result = Utility.handleCountyResponse(responseText);
        }
        //由于里面有UI更新，textview和listview更新，所以需要回到主线程操作
        if(result){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if("province".equals(type)){
                        queryProvinces();
                    } else if ("city".equals(type)) {
                        queryCities();
                    }else if("country".equals(type)){
                        queryCounties();
                    }
                }
            });
        }
    }
});

获取城市信息的步骤以及关键：
1.	首先需要三个常量标志位（以后程序有几种情况就用几个常量来表示），分别代表目前是哪个级别（省、市、县），然后对不同级别进行不同操作
LEVEL_PROVINCE、LEVEL_CITY、LEVEL_COUNTY
2.	采用碎片而不是活动，由于获取城市信息的功能很多地方都要用，所以写在fragment中，只要在activity中加入即可
Oncreate()和onActivityCreated()方法，在活动被创建的时候调用。
3.	首先从数据库中获取数据，如果没有（即第一次加载），那么就联网访问接口获取
4.	联网获取数据之后得到的是json字符串，然后需要进行解析，解析成实体类，然后实体类.save()方法。保存到数据库然后从数据库中获取数据进行显示
注意：访问网络的过程安卓6.0之后必须在子线程中，然后更新UI需要回到主线程。getActivity().runOnUiThread(new Runnable())
网址书写：http://guolin.tech/api/china/8
根据不同的级别进行不同的书写方式。
5.	Listview需要adapter，适配器需要datalist，重点就是获取这个list。从数据库查询的就是list，不同的情况，查询语句不同，后面需要有条件。
cityList = LitePal.where("provinceid= ?",String.valueOf(selectedProvince.getId())).find(City.class);
数据库采用Litepal，CURD操作依靠LitepalSupport类，以前的DataSupport已经废弃了。
6.	联网过程中需要progressDialog，show是显示，dismiss是关闭。


天气接口调用返回数据放进缓存中，并且显示
1.	由于天气接口返回的数据比较复杂，所以这里采用的是GSON处理数据，代码很简单，就是fromjson或者tojson，但是需要准备好具体的实体类。
实体类的编写，为了保留Android，java以及json的命名规则，这里采用以下注解进行标注。
@SerializedName("daily_forecast")
public List<Forecast> forecastList;
2.	天气布局：引入布局方式。使得整体比较工整。
 
3.	天气接口调用：分为不同的type，有不同的数据返回。

https://free-api.heweather.net/s6/weather/now?location=beijing&key=93ed3bc8991a4841bdf9d1122fc46bfc

或者：

https://free-api.heweather.net/s6/weather/now?location=CN101050101&key=93ed3bc8991a4841bdf9d1122fc46bfc

 
https://free-api.heweather.net/s6/weather/forecast?location=CN101050101&key=93ed3bc8991a4841bdf9d1122fc46bfc

 
https://free-api.heweather.net/s6/weather/lifestyle?location=CN101050101&key=93ed3bc8991a4841bdf9d1122fc46bfc
 
4.	GSON 处理数据：
主要用于处理较为复杂的数据，前提是准备好对应的实体类，其中要注意：字段名和json数据的节点名要对应，可以采用注解方式。
@SerializedName("cond_txt_d")
public String day;
然后:其中，Weather里面还有Forecast类的集合forecastList，直接会被封装成Forcast对象。
Weather forecastWeather = new Gson().fromJson(weatherContent, Weather.class);
weather.forecastList = forecastWeather.forecastList;
5.	网络请求和加载城市信息的一样，都是采用OKhttp进行网路请求，需要在子线程。传递网址参数。
6.	缓存：即轻量级的数据存储。可以放以前已经加载过的天气信息。
存数据。将网路请求过来的数据直接放进SharedPreferences：
SharedPreferences.Editor editor = MyApplication.getContext().getSharedPreferences("weather_data", MODE_PRIVATE).edit();
editor.putString(cate, responseText);
editor.apply();
获取缓存中的数据（键值对）
SharedPreferences preferences = MyApplication.getContext().getSharedPreferences("weather_data", MODE_PRIVATE);

为天气界面添加背景图，这里是FramLayout布局，因此，所有的控件都是从左上角开始布局的，因此后面的ScrollView会覆盖前面的imageview。实现背景图效果。
1.	Glide.with(this).load(bingPic).into(bingPicImg);
其中Glide是很强大的开源的图片处理工具，即可以加载本地的图片。也可以加载在线的图片（根据网址加载图片）
2.	如何实现透明、半透明…的状态栏。不采用引入design的方式。
https://www.jianshu.com/p/e89ee0a77bb5
记得在最后布局文件中加上：
 android:fitsSystemWindows="true"即设置title.xml与标题栏的边距。
 
3.	天气界面手动更新：采用下拉刷新，有具体的控件，androidx.swiperefreshlayout.widget.SwipeRefreshLayout
设置监听事件。
4.	切换城市：采用滑动侧边栏形式实现，androidx.drawerlayout.widget.DrawerLayout，第一个子控件是主布局，第二个子控件是侧边栏布局。
重新设置城市的点击事件，分情况，一个是mainactivity中的切换城市，跳转activity，如果是weatheractivity，不实现跳转了，因为主界面就是天气界面，实现重新请求天气即可。


