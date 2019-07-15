package com.example.blowdown_app;

import android.app.Application;
import android.app.Service;
import android.os.Vibrator;

import com.baidu.mapapi.SDKInitializer;
import com.example.blowdown_app.service.LocationUtil;

/**
 * 主Application,所有百度定位SDK的接口说明请参考线上文档：http://developer.baidu.com/map/loc_refer/index.html
 * <p>
 * 百度定位SDK官方网站：http://developer.baidu.com/map/index.php?title=android-locsdk
 * <p>
 * 直接拷贝com.baidu.location.service包到自己的工程下,简单配置即可获取定位结果,也可以根据demo内容自行封装
 */
public class LocationApplication extends Application
{
    public LocationUtil m_locationUtil;
    public Vibrator m_vibrator;

    @Override
    public void onCreate()
    {
        super.onCreate();
        /***
         * 初始化定位sdk,建议在Application中创建
         */
        m_locationUtil = new LocationUtil(getApplicationContext());
        m_vibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        SDKInitializer.initialize(getApplicationContext());

    }
}
