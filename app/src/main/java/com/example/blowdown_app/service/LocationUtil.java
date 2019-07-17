package com.example.blowdown_app.service;

import android.content.Context;
import android.os.Bundle;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.example.blowdown_app.MapActivity;
import com.example.blowdown_app.R;

import java.util.List;

public class LocationUtil
{
    private LocationClient m_locationClient;


    public LocationUtil()
    {
    }

    public LocationUtil(Context context)
    {
        //声明定位的类
        m_locationClient = new LocationClient(context);
    }

    /**
     * 取消监听
     *
     * @param listener
     */
    public void UnregisterListener(BDAbstractLocationListener listener)
    {
        if(listener != null)
        {
            m_locationClient.unRegisterLocationListener(listener);
        }
    }

    /**
     * 注册监听
     *
     * @param listener
     * @return
     */
    public boolean RegisterListener(BDAbstractLocationListener listener)
    {
        boolean isSuccess = false;
        if(listener != null)
        {
            m_locationClient.registerLocationListener(listener);
            isSuccess = true;
        }
        return isSuccess;
    }

    /**
     * 停止定位
     */
    public void Stop()
    {
        m_locationClient.stop();
    }

    /**
     * 启动定位
     * 自V7.2版本起,新增LocationClient.reStart()方法,用于在某些特定的异常环境下重启定位
     */
    public void Start()
    {
        SetLocationParam();
        m_locationClient.start();
    }

    /**
     * 通过参数配置可选择定位模式、可设定返回经纬度坐标类型、可设定是单次定位还是连续定位
     */
    private void SetLocationParam()
    {
        LocationClientOption option = new LocationClientOption();

        //可选,设置定位模式,默认高精度
        //LocationMode.Hight_Accuracy:高精度
        //LocationMode.Battery_Saving:低功耗
        //LocationMode.Device_Sensors:仅使用设备
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        //可选,设置返回经纬度坐标类型,默认GCJ02
        //GCJ02:国测局坐标;
        //BD09ll:百度经纬度坐标;
        //BD09:百度墨卡托坐标;
        //海外地区定位,无需设置坐标类型,统一返回WGS84类型坐标
        option.setCoorType("bd09ll");

        //可选,设置发起定位请求的间隔,int类型,单位ms
        //如果设置为0,则代表单次定位,即仅定位一次,默认为0
        //如果设置非0,需设置1000ms以上才有效
        option.setScanSpan(3000);

        //可选,是否需要地址信息,默认不需要
        option.setIsNeedAddress(true);

        //可选,默认false,设置是否需要位置语义化结果,可以在BDLocation.getLocationDescribe里得到,结果类似于“在北京天安门附近”
        option.setIsNeedLocationDescribe(true);

        //可选,设置是否需要设备方向结果
        option.setNeedDeviceDirect(false);

        //可选,默认false,设置是否需要POI结果,可以在BDLocation.getPoiList里得到
        option.setIsNeedLocationPoiList(true);

        //可选,默认false,设置定位时是否需要海拔信息,默认不需要,除基础定位版本都可用
        option.setIsNeedAltitude(false);

        //可选,设置是否使用GPS,默认false,使用高精度和仅用设备两种定位模式的,参数必须设置为true
        option.setOpenGps(true);

        //可选,设置是否当GPS有效时按照1S/1次频率输出GPS结果,默认false
        option.setLocationNotify(true);

        //可选,定位SDK内部是一个service,并放到了独立进程,设置是否在stop的时候杀死这个进程,默认（建议）不杀死,即setIgnoreKillProcess(true)
        option.setIgnoreKillProcess(false);

        //可选,设置是否收集Crash信息,默认收集,即参数为false
        option.SetIgnoreCacheException(false);

        //可选,V7.2版本新增能力,如果设置了该接口,首次启动定位时,会先判断当前Wi-Fi是否超出有效期,若超出有效期,会先重新扫描Wi-Fi,然后定位
        option.setWifiCacheTimeOut(5 * 60 * 1000);

        //可选,设置是否需要过滤GPS仿真结果,默认需要,即参数为false
        option.setEnableSimulateGps(false);

        //m_locationClient为初始化过的LocationClient对象,需将配置好的LocationClientOption对象,通过setLocOption方法传递给LocationClient对象使用,
        // 更多LocationClientOption的配置,请参照类参考中LocationClientOption类的详细说明
        m_locationClient.setLocOption(option);
    }

}