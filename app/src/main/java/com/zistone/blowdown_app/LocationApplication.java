package com.zistone.blowdown_app;

import android.app.Application;
import android.app.Service;
import android.content.SharedPreferences;
import android.os.Vibrator;

import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.model.OnCustomAttributeListener;

import java.util.HashMap;
import java.util.Map;

/**
 * 主Application,所有百度定位SDK的接口说明请参考线上文档：http://developer.baidu.com/map/loc_refer/index.html
 * <p>
 * 百度定位SDK官方网站：http://developer.baidu.com/map/index.php?title=android-locsdk
 * <p>
 * 直接拷贝com.baidu.location.service包到自己的工程下,简单配置即可获取定位结果,也可以根据demo内容自行封装
 */
public class LocationApplication extends Application
{
    /**
     * 轨迹客户端
     */
    public LBSTraceClient m_traceClient = null;

    @Override
    public void onCreate()
    {
        super.onCreate();
        //在使用SDK各组间之前初始化context信息,传入ApplicationContext
        //默认本地个性化地图初始化方法
        SDKInitializer.initialize(this);

        //自4.3.0起,百度地图SDK所有接口均支持百度坐标和国测局坐标,用此方法设置您使用的坐标类型
        //包括BD09LL和GCJ02两种坐标,默认是BD09LL坐标
        SDKInitializer.setCoordType(CoordType.BD09LL);

        m_traceClient = new LBSTraceClient(getApplicationContext());
        m_traceClient.setOnCustomAttributeListener(new OnCustomAttributeListener()
        {
            @Override
            public Map<String, String> onTrackAttributeCallback()
            {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }

            @Override
            public Map<String, String> onTrackAttributeCallback(long locTime)
            {
                System.out.println("onTrackAttributeCallback, locTime : " + locTime);
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }
        });
    }

}
