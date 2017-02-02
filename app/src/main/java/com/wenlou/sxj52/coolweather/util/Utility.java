package com.wenlou.sxj52.coolweather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.wenlou.sxj52.coolweather.db.City;
import com.wenlou.sxj52.coolweather.db.County;
import com.wenlou.sxj52.coolweather.db.Province;
import com.wenlou.sxj52.coolweather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sxj52 on 2017/2/2.
 */

public class Utility {
    /**
     *
     * @param 解析和处理服务器返回的省级数据
     * @return
     */
    public static boolean handlerProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    Province province=new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *
     * @param 解析和处理服务器返回的时级数据
     * @param provinceId
     * @return
     */
    public static boolean handlerCityResponse(String response,int  provinceId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    City city=new City();
                    city.setCityName(provinceObject.getString("name"));
                    city.setCityCode(provinceObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     *
     * @param 解析和处理服务器返回的县级数据
     * @param cityId 城市代码
     * @return
     */
    public static boolean handlerCoutyResponse(String response,int  cityId){
        if(!TextUtils.isEmpty(response)){
            try {
                JSONArray allProvinces=new JSONArray(response);
                for(int i=0;i<allProvinces.length();i++){
                    JSONObject provinceObject=allProvinces.getJSONObject(i);
                    County county=new County();
                    county.setCountyName(provinceObject.getString("name"));
                    county.setCityId(cityId);
                    county.setWeatherId(provinceObject.getString("weather_id"));
                    county.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Weather handlerWeatherResponse(String response){
        try {
            JSONObject jsonObject=new JSONObject(response);
            JSONArray jsonArray=jsonObject.getJSONArray("HeWeather");
            String weatherContent=jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}
