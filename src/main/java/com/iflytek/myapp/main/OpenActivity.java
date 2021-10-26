package com.iflytek.myapp.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.iflytek.domain.MyView.CustomVideoView;
import com.iflytek.myapp.R;
import com.iflytek.myapp.base.BaseActivity;
import com.show.api.ShowApiRequest;

/**
 * 这是开机模块，csdn上复制下来的，主要功能就是加载一个开机动画，加载完后进入主界面Main_New_Activity
 * IOS也去搜一下复制一个就行。
 *
 * 在这个模块还初始化了天气，initWeather中通过api获取了天气，并保存起来了，以便之后用户查询天气，不需要
 * 每次都访问网络来获取。
 */

public class OpenActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open);
        initWeather();
        initView();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainintent = new Intent(OpenActivity.this, Main_New_Activity.class); //使用intent方法，在活动间跳转
                startActivity(mainintent);
                finish();
            }
        }, 3500);//设置等待时间，与跳转。


    }

    private void initView() {
        CustomVideoView videoview = this.findViewById(R.id.welcome_video);
        //设置播放加载路径
        videoview.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.start));
        //播放
        videoview.start();
    }

    /**
     * 初始化天气并保存，由于这个api返回的天气是json格式，因此需要用JSON来解析数据，从中提取出需要的天气数据，
     * 我这里用的是阿里巴巴的fastjson，提取了地区，风速，风向，温度，天气等信息，然后保存起来。
     * 开发IOS的时候看看有没有相似的api，按照我这个提取方法来提取就行。
     */

    private void initWeather() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String res = new ShowApiRequest("http://route.showapi.com/9-4", getString(R.string.news_username), getString(R.string.news_password))
                        .post();

                /////////////////////////解析得到每个对象///////////////////////////
                JSONObject parse = (JSONObject) JSON.parse(res);        //将整个json文件转换成对象parse
                JSONObject showapi_res_body = (JSONObject) parse.get("showapi_res_body");   //获取parse对象中的“showapi_res_body”对象
                JSONObject now = (JSONObject) showapi_res_body.get("now");      //获取showapi_res_body中的now对象
                JSONObject aqiDetail = (JSONObject) now.get("aqiDetail");       //获取now中的aqiDetail对象
                JSONObject cityInfo = (JSONObject) showapi_res_body.get("cityInfo");    //获取showapi_res_body中的cityInfo对象


                ////////////////////获取各个信息////////////////////////////
                String temperature = now.getString("temperature");
                String area = aqiDetail.getString("area");
                String wind_direction = now.getString("wind_direction");
                String wind_power = now.getString("wind_power");
                String province = cityInfo.getString("c7");
                String weather = now.getString("weather");


                //////////////////////获取存储文件/////////////////////////
                SharedPreferences sharedPreferences = getSharedPreferences("message", MODE_PRIVATE);
                SharedPreferences.Editor edit = sharedPreferences.edit();



                /////////////////////////在存储文件中保存所有的信息//////////////////////
                edit.putString("temperature", temperature);
                edit.putString("area", area);
                edit.putString("wind_direction", wind_direction);
                edit.putString("wind_power", wind_power);
                edit.putString("province", province);
                edit.putString("weather", weather);
                edit.apply();
            }
        }).start();
    }


}
