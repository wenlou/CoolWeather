package com.wenlou.sxj52.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.wenlou.sxj52.coolweather.gson.Weather;
import com.wenlou.sxj52.coolweather.util.HttpUtil;
import com.wenlou.sxj52.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
      return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        AlarmManager manger= (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour=8*60*60*1000;
        long triggerAtTime= SystemClock.elapsedRealtime()+anHour;
        Intent i=new Intent(this,AutoUpdateService.class);
        PendingIntent pi=PendingIntent.getService(this,0,i,0);
        manger.cancel(pi);
        manger.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        String requestBingPic="http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpREquest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingpic=response.body().string();
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this).edit();
                editor.putString("bing_pic",bingpic);
                editor.apply();
            }
        });
    }

    private void updateWeather() {
        SharedPreferences sp= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=sp.getString("weather",null);

        if(weatherString!=null){
            Weather weather= Utility.handlerWeatherResponse(weatherString);
            String weatherId=weather.basic.id;
            String weatherUrl="http://guolin.tech/api/weather?cityid="+weatherId+"&key=e2faff6b359440c288e7fb675df53d22";
            HttpUtil.sendOkHttpREquest(weatherUrl, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                   e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseText=response.body().string();
                    Weather weather= Utility.handlerWeatherResponse(responseText);
                    if(weather!=null&&"ok".equals(weather.status)) {
                        SharedPreferences.Editor editor = PreferenceManager.
                                getDefaultSharedPreferences(AutoUpdateService.this).edit();
                        editor.putString("weather", responseText);
                        editor.apply();
                    }
                }
            });
        }

    }
}
