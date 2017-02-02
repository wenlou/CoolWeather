package com.wenlou.sxj52.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by sxj52 on 2017/2/2.
 */

public class HttpUtil {
    public static void sendOkHttpREquest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
