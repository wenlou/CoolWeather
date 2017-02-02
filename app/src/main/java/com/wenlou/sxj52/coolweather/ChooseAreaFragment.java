package com.wenlou.sxj52.coolweather;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenlou.sxj52.coolweather.db.City;
import com.wenlou.sxj52.coolweather.db.County;
import com.wenlou.sxj52.coolweather.db.Province;
import com.wenlou.sxj52.coolweather.util.HttpUtil;
import com.wenlou.sxj52.coolweather.util.Utility;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by sxj52 on 2017/2/2.
 */

public class ChooseAreaFragment extends Fragment{
    public static final int LEVEL_PROVINCE=0;
    public static final int LEVEL_CITY=1;
    public static final int LEVEL_COUNTY=2;
    private ProgressDialog mProgressDialog;
    private TextView titleText;
    private Button backButton;
    private ListView mListView;
    private ArrayAdapter<String> mAdapter;
    private List<String> dataList=new ArrayList<String>();
    //省列表
    private List<Province> mProvinceList;
    //市列表
    private List<City> mCityList;
    //县列表
    private List<County> mCountyList;
    //选中的省
    private Province selectedProvince;
    //选中的市
    private City selectCity;
    //当前选中的级别
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.choose_area,container,false);
        titleText= (TextView) view.findViewById(R.id.title_text);
        backButton= (Button) view.findViewById(R.id.back_button);
        mListView= (ListView) view.findViewById(R.id.list_view);
        mAdapter=new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,dataList);
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel==LEVEL_PROVINCE){
                    selectedProvince=mProvinceList.get(position);
                    queryCites();
                }else if(currentLevel==LEVEL_CITY){
                    selectCity=mCityList.get(position);
                    queryCounties();
                }
                else if(currentLevel==LEVEL_COUNTY){
                  String weatherId=mCountyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivty.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivty){
                        WeatherActivty activty= (WeatherActivty) getActivity();
                        activty.drawLayout.closeDrawers();
                        activty.swipeRefresh.setRefreshing(true);
                        activty.weatherId=weatherId;
                        activty.requestWeather(weatherId);
                    }
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentLevel==LEVEL_COUNTY){
                    queryCites();
                }else if(currentLevel==LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    /**
     * 查询全国的省，优先从数据库里查找，如果没有去服务器上查
     */
    private void queryProvinces() {
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        mProvinceList= DataSupport.findAll(Province.class);
        if(mProvinceList.size()>0){
            dataList.clear();
            for(Province province:mProvinceList){
                dataList.add(province.getProvinceName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_PROVINCE;
        }else{
            String address="http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    /**
     * 查询市内的县，优先从数据库里查找，如果没有去服务器上查
     */

    private void queryCounties() {
        titleText.setText(selectCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        mCountyList=DataSupport.where("cityid=?",String.valueOf(selectCity.getId())).find(County.class);
        if(mCountyList.size()>0){
            dataList.clear();
            for(County county:mCountyList){
                dataList.add(county.getCountyName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_COUNTY;
        }
        else{
            int provinceCode=selectedProvince.getProvinceCode();
            int CityCode=selectCity.getCityCode();
            String address="http://guolin.tech/api/china/"+provinceCode+"/"+CityCode;
            queryFromServer(address,"county");
        }
    }
    /**
     * 查询省里的市，优先从数据库里查找，如果没有去服务器上查
     */
    private void queryCites() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        mCityList=DataSupport.where("provinceid=?",String.valueOf(selectedProvince.getId())).find(City.class);
        if(mCityList.size()>0){
            dataList.clear();
            for(City city:mCityList){
                dataList.add(city.getCityName());
            }
            mAdapter.notifyDataSetChanged();
            mListView.setSelection(0);
            currentLevel=LEVEL_CITY;
        }
        else{
            int provinceCode=selectedProvince.getProvinceCode();
            String address="http://guolin.tech/api/china/"+provinceCode;
            queryFromServer(address,"city");
        }

    }

    /**
     * 根据传入的地址和类型从服务器上查询省县市的数据
     * @param address
     * @param type
     */
    private void queryFromServer(String address, final String type) {
        showProgessDialog();
        HttpUtil.sendOkHttpREquest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                  String responseText=response.body().string();
                  boolean result=false;
                 if("province".equals(type)){
                     result= Utility.handlerProvinceResponse(responseText);
                 }else if("city".equals(type)){
                     result=Utility.handlerCityResponse(responseText,selectedProvince.getId());
                 }else if("county".equals(type)){
                     result=Utility.handlerCoutyResponse(responseText,selectCity.getId());
                 }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("city".equals(type)){
                               queryCites();
                            }else if("county".equals(type)){
                              queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }

    private void closeProgressDialog() {
        if(mProgressDialog!=null){
            mProgressDialog.dismiss();
        }
    }

    private void showProgessDialog() {
        if(mProgressDialog==null){
            mProgressDialog=new ProgressDialog(getActivity());
            mProgressDialog.setMessage("正在加载。。。。。");
            mProgressDialog.setCanceledOnTouchOutside(false);
        }
        mProgressDialog.show();
    }
}
