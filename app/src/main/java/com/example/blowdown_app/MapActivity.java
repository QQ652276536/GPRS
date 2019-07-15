package com.example.blowdown_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.map.BaiduMap;
import com.example.blowdown_app.service.LocationUtil;

import java.util.List;

public class MapActivity extends AppCompatActivity
{
    private LocationUtil m_locationUtil;
    private TextView m_text;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        m_text = findViewById(R.id.textView);
        m_text.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    @Override
    protected void onStop()
    {
        //注销监听
        m_locationUtil.UnregisterListener(m_myLocationListener);
        //停止定位服务
        m_locationUtil.Stop();
        super.onStop();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        //建议应用中只初始化一个LocationUtil实例,然后使用
        m_locationUtil = ((LocationApplication) getApplication()).m_locationUtil;
        //注册监听
        m_locationUtil.RegisterListener(m_myLocationListener);
        //程序启动时就开始定位
        m_locationUtil.Start();
    }

    /**
     * 显示定位结果字符串
     *
     * @param str
     */
    public void PrintLocationResult(String str)
    {
        final String finalStr = str;
        try
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    m_text.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            m_text.setText(finalStr);
                        }
                    });

                }
            }).start();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 定位结果回调
     */
    private BDAbstractLocationListener m_myLocationListener = new BDAbstractLocationListener()
    {
        @Override
        public void onReceiveLocation(BDLocation location)
        {
            //此处的BDLocation为定位结果信息类,通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息更多结果信息获取说明,请参照类参考中BDLocation类中的说明
            //BDLocation.TypeServerError:服务端定位失败,请您检查是否禁用获取位置信息权限,尝试重新请求定位
            if(null != location && location.getLocType() != BDLocation.TypeServerError)
            {
                StringBuffer sb = new StringBuffer(256);
                //时间也可以使用systemClock.elapsedRealtime()方法,获取的是自从开机以来每次回调的时间;
                //location.getTime()是指服务端出本次结果的时间,如果位置不发生变化,则时间不变
                sb.append("时间 : ");
                sb.append(location.getTime());
                //获取纬度信息
                double latitude = location.getLatitude();
                sb.append("\n纬度 : ");
                sb.append(latitude);
                //获取经度信息
                double longitude = location.getLongitude();
                sb.append("\n经度 : ");
                sb.append(longitude);
                //获取定位精度,默认值为0.0f
                float radius = location.getRadius();
                sb.append("\n精度 : ");
                sb.append(radius);
                //获取经纬度坐标类型,以LocationClientOption中设置过的坐标类型为准
                String coorType = location.getCoorType();
                sb.append("\n坐标类型 : ");
                sb.append(coorType);
                //获取定位类型、定位错误返回码,具体信息可参照类参考中BDLocation类中的说明
                int errorCode = location.getLocType();
                sb.append("\n返回码 : ");
                sb.append(errorCode);
                //获取详细地址信息
                String addr = location.getAddrStr();
                sb.append("\n详细地址 : ");
                sb.append(addr);
                //获取国家
                String country = location.getCountry();
                sb.append("\n国家 : ");
                sb.append(country);
                //获取省份
                String province = location.getProvince();
                sb.append("\n省份 : ");
                sb.append(province);
                //获取城市
                String city = location.getCity();
                sb.append("\n城市 : ");
                sb.append(city);
                //获取区县
                String district = location.getDistrict();
                sb.append("\n区县 : ");
                sb.append(district);
                //获取街道信息
                String street = location.getStreet();
                sb.append("\n街道 : ");
                sb.append(street);
                //获取位置描述信息
                String locationDescribe = location.getLocationDescribe();
                sb.append("\n位置描述 : ");
                sb.append(locationDescribe);
                //获取周边POI信息
                List<Poi> poiList = location.getPoiList();
                //POI信息包括POI ID、名称等,具体信息请参照类参考中POI类的相关说明
                if(poiList != null)
                {
                    for(Poi poi : poiList)
                    {
                    }
                }
                //GPS定位结果
                if(location.getLocType() == BDLocation.TypeGpsLocation)
                {
                    sb.append("\n速度(km/h) : ");
                    sb.append(location.getSpeed());
                    sb.append("\n卫星数目 : ");
                    sb.append(location.getSatelliteNumber());
                    sb.append("\n海拔高度(米) : ");
                    sb.append(location.getAltitude());
                    sb.append("\nGPS质量判断 : ");
                    sb.append(location.getGpsAccuracyStatus());
                    sb.append("GPS定位成功");
                }
                //网络定位结果
                else if(location.getLocType() == BDLocation.TypeNetWorkLocation)
                {
                    //如果有海拔高度
                    if(location.hasAltitude())
                    {
                        sb.append("\n海拔(米) : ");
                        sb.append(location.getAltitude());
                    }
                    //运营商信息
                    sb.append("\n运营商信息 : ");
                    sb.append(location.getOperators());
                    sb.append("网络定位成功");
                }
                //离线定位结果
                else if(location.getLocType() == BDLocation.TypeOffLineLocation)
                {
                    sb.append("离线定位成功,离线定位结果也是有效的");
                }
                else if(location.getLocType() == BDLocation.TypeServerError)
                {
                    sb.append("服务端网络定位失败,可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com,会有人追查原因");
                }
                else if(location.getLocType() == BDLocation.TypeNetWorkException)
                {
                    sb.append("网络不同导致定位失败,请检查网络是否通畅");
                }
                else if(location.getLocType() == BDLocation.TypeCriteriaException)
                {
                    sb.append("无法获取有效定位依据导致定位失败,一般是由于手机的原因,处于飞行模式下一般会造成这种结果,可以试着重启手机");
                }
                PrintLocationResult(sb.toString());
            }
        }
    };

}
