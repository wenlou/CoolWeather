package com.wenlou.sxj52.coolweather;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wenlou.sxj52.coolweather.gson.Weather;
import com.wenlou.sxj52.coolweather.service.AutoUpdateService;
import com.wenlou.sxj52.coolweather.util.HttpUtil;
import com.wenlou.sxj52.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivty extends AppCompatActivity {
    private ScrollView mScrollView;
    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private LinearLayout forecastLayout;
    private TextView apiText;
    private TextView pm25Text;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private ImageView bigPicImg;
    public SwipeRefreshLayout swipeRefresh;
    private Button navButton;
    public DrawerLayout drawLayout;
    public  String weatherId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>=21){
            View decorView=getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activty_weather);
        mScrollView= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_updtate_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        apiText= (TextView) findViewById(R.id.api_text);
        pm25Text= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sportText= (TextView) findViewById(R.id.sport_text);
        bigPicImg= (ImageView) findViewById(R.id.bing_pic_img);
        navButton= (Button) findViewById(R.id.nav_button);
        drawLayout= (DrawerLayout) findViewById(R.id.darw_layout);
        swipeRefresh= (SwipeRefreshLayout) findViewById(R.id.swip_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawLayout.openDrawer(GravityCompat.START);
            }
        });
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sp.getString("weather",null);

        if(weatherString!=null){
            Weather weather= Utility.handlerWeatherResponse(weatherString);
            weatherId=weather.basic.id;
            showWeatherInfo(weather);
        }
        else{
            weatherId=getIntent().getStringExtra("weather_id");
            mScrollView.setVisibility(View.VISIBLE);
            requestWeather(weatherId);
        }
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        String bingpic=sp.getString("bing_pic",null);
        if(bingpic!=null){
            Glide.with(this).load(bingpic).into(bigPicImg);
        }else{
            loadBingPic();
        }
    }

    private void loadBingPic() {
        final String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpREquest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 final String bingpic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(WeatherActivty.this).edit();
                editor.putString("bing_pic",bingpic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivty.this).load(bingpic).into(bigPicImg);
                    }
                });
            }
        });
    }

    public void requestWeather(final String weatherId) {
        String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=e2faff6b359440c288e7fb675df53d22";
        HttpUtil.sendOkHttpREquest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivty.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                 final String responseText=response.body().string();
                final Weather weather=Utility.handlerWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(weather!=null&&"ok".equals(weather.status)){
                            SharedPreferences.Editor editor=PreferenceManager.
                                    getDefaultSharedPreferences(WeatherActivty.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else{
                            Toast.makeText(WeatherActivty.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    private void showWeatherInfo(Weather weather) {
        String cityName=weather.basic.city;
        String updataTime=weather.basic.update.utc.split(" ")[1];
        String degree=weather.now.tmp+"ºc";
        String weatherInfo=weather.now.cond.txt;
        titleCity.setText(cityName);
        degreeText.setText(degree);
        titleUpdateTime.setText(updataTime);
        weatherInfoText.setText(weatherInfo);
        forecastLayout.removeAllViews();
        for(Weather.DailyForecastEntity forecast: weather.dailyForecast){
            View view= LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText= (TextView) view.findViewById(R.id.data_text);
            TextView infoText= (TextView) view.findViewById(R.id.info_text);
            TextView maxText= (TextView) view.findViewById(R.id.max_text);
            TextView minText= (TextView) view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.cond.txtD);
            maxText.setText(forecast.tmp.max);
            minText.setText(forecast.tmp.min);
            forecastLayout.addView(view);
        }
        if(weather.aqi!=null){
            apiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort="舒适度:"+weather.suggestion.comf.txt;
        String carWash="洗车指数:"+weather.suggestion.cw.txt;
        String sport="运动建议:"+weather.suggestion.sport.txt;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);
        mScrollView.setVisibility(View.VISIBLE);
        Intent intent = new Intent(this, AutoUpdateService.class);
        startService(intent);
    }
}
