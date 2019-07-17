package com.zistone.blowdown_app.fragment;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.zistone.blowdown_app.R;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL = "http://10.0.2.2:8080/Blowdown/UserInfo/Login";

    private String mParam1;
    private String mParam2;

    private MyLocationListener m_locationListener = new MyLocationListener();
    private LocationClient m_locationClient;
    private View m_mapView;
    private TextView m_textView;
    private MapView m_baiduMapView;
    private BaiduMap m_baiduMap;
    private SensorManager m_sensorManager;
    //是否首次定位
    private boolean m_isFirstLoc = true;
    private double m_lastX;
    private MyLocationData m_locationData;
    private int m_currentDirection;
    //当前纬度
    private double m_currentLat;
    //当前经度
    private double m_currentLon;
    //当前定位精度
    private float m_currentAccracy;
    private boolean m_isPermissionRequested;
    private SDKReceiver m_sdkReceiver;

    private OnFragmentInteractionListener mListener;

    public MapFragment()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(String param1, String param2)
    {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 定位结果回调
     */
    public class MyLocationListener extends BDAbstractLocationListener
    {
        @Override
        public void onReceiveLocation(BDLocation location)
        {
            //此处的BDLocation为定位结果信息类,通过它的各种get方法可获取定位相关的全部结果
            //以下只列举部分获取经纬度相关（常用）的结果信息更多结果信息获取说明,请参照类参考中BDLocation类中的说明
            //BDLocation.TypeServerError:服务端定位失败,请您检查是否禁用获取位置信息权限,尝试重新请求定位
            if (null == m_baiduMapView || null == location || BDLocation.TypeServerError == location.getLocType())
            {
                return;
            }
            StringBuffer sb = new StringBuffer(256);
            //时间也可以使用systemClock.elapsedRealtime()方法,获取的是自从开机以来每次回调的时间;
            //location.getTime()是指服务端出本次结果的时间,如果位置不发生变化,则时间不变
            sb.append("时间 : ");
            sb.append(location.getTime());
            //获取纬度信息
            double latitude = location.getLatitude();
            m_currentLat = latitude;
            sb.append("\n纬度 : ");
            sb.append(latitude);
            //获取经度信息
            double longitude = location.getLongitude();
            m_currentLon = longitude;
            sb.append("\n经度 : ");
            sb.append(longitude);
            //获取定位精度,默认值为0.0f
            float radius = location.getRadius();
            m_currentAccracy = radius;
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
            if (poiList != null)
            {
                for (Poi poi : poiList)
                {
                }
            }
            //GPS定位结果
            if (location.getLocType() == BDLocation.TypeGpsLocation)
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
            else if (location.getLocType() == BDLocation.TypeNetWorkLocation)
            {
                //如果有海拔高度
                if (location.hasAltitude())
                {
                    sb.append("\n海拔(米) : ");
                    sb.append(location.getAltitude());
                }
                //运营商信息
                sb.append("\n运营商信息 : ");
                sb.append(location.getOperators());
                sb.append("\n网络定位成功");
            }
            //离线定位结果
            else if (location.getLocType() == BDLocation.TypeOffLineLocation)
            {
                sb.append("离线定位成功,离线定位结果也是有效的");
            }
            else if (location.getLocType() == BDLocation.TypeServerError)
            {
                sb.append("服务端网络定位失败,可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com,会有人追查原因");
            }
            else if (location.getLocType() == BDLocation.TypeNetWorkException)
            {
                sb.append("网络不同导致定位失败,请检查网络是否通畅");
            }
            else if (location.getLocType() == BDLocation.TypeCriteriaException)
            {
                sb.append("无法获取有效定位依据导致定位失败,一般是由于手机的原因,处于飞行模式下一般会造成这种结果,可以试着重启手机");
            }
            //将定位信息显示在TextView
            //PrintLocationResult(sb.toString());
            m_locationData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(m_currentDirection).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            //此处设置开发者获取到的方向信息,顺时针0-360
            m_baiduMap.setMyLocationData(m_locationData);
            //如果是首次定位
            if (m_isFirstLoc)
            {
                m_isFirstLoc = false;
                //根据经纬度定位位置
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                m_baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    /**
     * 构造广播监听类,监听SDK的Key验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action))
            {
                return;
            }
            //鉴权错误信息描述
            m_textView.setTextColor(Color.RED);
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR))
            {
                m_textView.setText("Key验证出错!错误码:"
                        + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        + ";错误信息:"
                        + intent.getStringExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_MESSAGE));
            }
            else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR))
            {
                m_textView.setText("网络出错");
            }
            else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK))
            {
                m_textView.setText("Key验证成功!功能可以正常使用");
                m_textView.setTextColor(Color.GREEN);
                m_textView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 通过参数配置可选择定位模式、可设定返回经纬度坐标类型、可设定是单次定位还是连续定位等等
     * 更多LocationClientOption的配置,请参照类参考中LocationClientOption类的详细说明
     */
    private LocationClientOption SetLocationClientOption(LocationClientOption option)
    {
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

        return option;
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void RequestPermission()
    {
        if (Build.VERSION.SDK_INT >= 23 && !m_isPermissionRequested)
        {
            m_isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.ACCESS_WIFI_STATE,
            };
            for (String perm : permissions)
            {
                if (PackageManager.PERMISSION_GRANTED != getContext().checkSelfPermission(perm))
                {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            if (!permissionsList.isEmpty())
            {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }

    private void InitData()
    {
        m_textView = m_mapView.findViewById(R.id.textView);
        m_baiduMapView = m_mapView.findViewById(R.id.mapView);
        //动态获取权限
        RequestPermission();
        //注册SDK广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        m_sdkReceiver = new SDKReceiver();
        getContext().registerReceiver(m_sdkReceiver, iFilter);
        //获取传感器管理服务
        m_sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        //支持TextView内容滑动
        m_textView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //地图初始化
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        m_baiduMap = m_baiduMapView.getMap();
        m_baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        //定位模式:罗盘
        MyLocationConfiguration locationConfiguration = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, true, null);
        m_baiduMap.setMyLocationConfiguration(locationConfiguration);
        //开启定位图层
        m_baiduMap.setMyLocationEnabled(true);
        //定位初始化
        m_locationClient = new LocationClient(getContext());
        m_locationClient.registerLocationListener(m_locationListener);
        //设置坐标各项参数
        LocationClientOption option = SetLocationClientOption(new LocationClientOption());
        m_locationClient.setLocOption(option);
        m_locationClient.start();
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * 创建Fragment时回调,只会被调用一次
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * 绘制Fragment组件时回调
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_mapView = inflater.inflate(R.layout.fragment_map, container, false);
        InitData();
        return m_mapView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }
}