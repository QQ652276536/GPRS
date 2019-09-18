package com.zistone.blowdown_app.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment extends Fragment implements BaiduMap.OnMapClickListener, View.OnClickListener, SensorEventListener, OnGetGeoCoderResultListener, Serializable, BaiduMap.OnMarkerClickListener
{
    private static final String TAG = "MapFragment";
    private static final BitmapDescriptor ICON_MARKER = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark2);
    private Context m_context;
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
    //设备标记
    private Marker m_marker;
    //经纬度对应的详细信息
    private String m_latLngStr = "";
    //地理编码搜索
    private GeoCoder m_geoCoder;
    //设备的经纬度
    private LatLng m_latLng;
    //设备信息
    private DeviceInfo m_deviceInfo;
    private Activity m_activity;
    private OnFragmentInteractionListener mListener;
    private LinearLayout m_infoWindow;
    private ImageButton m_btnLocation;
    private ImageButton m_btnTraffic;
    private ImageButton m_btnLocus;
    private ImageButton m_btnTask;
    private ImageButton m_btnDefense;
    private boolean m_trafficEnabled;
    private Button m_btnMonitorTarget;

    public static MapFragment newInstance(DeviceInfo deviceInfo)
    {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void CreateInfoWindow()
    {
        TextView textName = m_infoWindow.findViewById(R.id.textView4_map_device_info);
        TextView textSIM = m_infoWindow.findViewById(R.id.textView5_map_device_info);
        TextView textUpMode = m_infoWindow.findViewById(R.id.textView8_map_device_info);
        TextView textUpInterval = m_infoWindow.findViewById(R.id.textView10_map_device_info);
        TextView textReceiveTime = m_infoWindow.findViewById(R.id.textView12_map_device_info);
        TextView textLat = m_infoWindow.findViewById(R.id.textView14_map_device_info);
        TextView textLocation = m_infoWindow.findViewById(R.id.textView16_map_device_info);
        TextView textTemperature = m_infoWindow.findViewById(R.id.textView18_map_device_info);
        TextView textLastEct = m_infoWindow.findViewById(R.id.textView20_map_device_info);
        textName.setText(m_deviceInfo.getM_name());
        textSIM.setText(String.valueOf(m_deviceInfo.getM_sim()));
        textUpMode.setText("正常模式");
        textUpInterval.setText("10分钟/次");
        textReceiveTime.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_deviceInfo.getM_updateTime()));
        textLat.setText(m_deviceInfo.getM_lot() + ", " + m_deviceInfo.getM_lat());
        textLocation.setText(m_latLngStr);
        textTemperature.setText("20℃");
        textLastEct.setText("80%");
        m_baiduMap.showInfoWindow(new InfoWindow(m_infoWindow, m_latLng, -100));
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        if (null != m_latLng && null != m_deviceInfo)
        {
            CreateInfoWindow();
        }
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if (null != m_infoWindow)
        {
            m_baiduMap.hideInfoWindow();
            m_baiduMapView.postInvalidate();
        }
    }

    @Override
    public boolean onMapPoiClick(MapPoi mapPoi)
    {
        return false;
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 创建平移动画
     */
    private Animation Transformation()
    {
        Point point = m_baiduMap.getProjection().toScreenLocation(m_latLng);
        LatLng latLng = m_baiduMap.getProjection().fromScreenLocation(new Point(point.x, point.y - 30));
        Transformation mTransforma = new Transformation(m_latLng, latLng, m_latLng);
        mTransforma.setDuration(500);
        //动画重复模式
        mTransforma.setRepeatMode(Animation.RepeatMode.RESTART);
        //动画重复次数
        mTransforma.setRepeatCount(2);
        mTransforma.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart()
            {
            }

            @Override
            public void onAnimationEnd()
            {
            }

            @Override
            public void onAnimationCancel()
            {
            }

            @Override
            public void onAnimationRepeat()
            {
            }
        });
        return mTransforma;
    }

    /**
     * 监听定位结果
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
            StringBuffer sb = new StringBuffer();
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
                    Log.i(TAG, ">>>" + poi.getId() + "\t" + poi.getName() + "\t" + poi.getRank());
                }
            }
            Log.i(TAG, ">>>" + sb.toString());
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
                m_textView.setText("Key验证出错!错误码:" + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0) + ";错误信息:" + intent.getStringExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_MESSAGE));
                m_textView.setTextColor(Color.RED);
                m_textView.setVisibility(View.INVISIBLE);
            }
            else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR))
            {
                m_textView.setText("网络出错");
                m_textView.setTextColor(Color.RED);
                m_textView.setVisibility(View.INVISIBLE);
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
                    Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_SETTINGS, Manifest.permission.ACCESS_WIFI_STATE,
            };
            for (String perm : permissions)
            {
                if (PackageManager.PERMISSION_GRANTED != m_context.checkSelfPermission(perm))
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

    /**
     * 设置地图状态和标记的位置
     */
    private void SetMapStateAndMarkOptions()
    {
        if (null == m_latLng)
        {
            return;
        }
        //设置标记的位置和图标
        MarkerOptions markerOptions = new MarkerOptions().position(m_latLng).icon(ICON_MARKER);
        //标记添加至地图中
        m_marker = (Marker) (m_baiduMap.addOverlay(markerOptions));
        //定义地图缩放级别3~16,值越大地图越精细
        MapStatus mapStatus = new MapStatus.Builder().target(m_latLng).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //改变地图状态
        m_baiduMap.setMapStatus(mapStatusUpdate);
        //添加平移动画
        m_marker.setAnimation(Transformation());
        m_marker.startAnimation();
    }

    /**
     * 百度地图API提供的绘制文字有中心点偏差,导致效果很不理想,为了解决这个问题,可以采用TextView渲染Bitmap然后添加为图标覆盖物的方式,这样既可以实现换行,也可以控制中心点
     * ,实现正常的地图旋转等效果
     * <p>
     * 使用该方式绘制文字时不允许为"",否则会抛BDMapSDKException: marker's icon can not be null
     *
     * @param latLng
     * @param str
     * @param foreColor
     * @param backColor
     */
    private void DrawMarkerText(LatLng latLng, String str, int foreColor, int backColor)
    {
        if (null == latLng || "".equals(str) || null == m_activity)
        {
            return;
        }
        TextView textView = new TextView(m_activity);
        //内容距中
        textView.setGravity(Gravity.LEFT);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        textView.setBackgroundColor(backColor);
        textView.setTextColor(foreColor);
        textView.setText(str);
        //更新绘制缓存之前把旧的销毁
        textView.destroyDrawingCache();
        //通过测量实现TextView和文字的大小一致
        textView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, textView.getMeasuredWidth(), textView.getMeasuredHeight());
        //启用绘制缓存
        textView.setDrawingCacheEnabled(true);
        //将View的内容以图片的方式保存
        Bitmap bitmapText = textView.getDrawingCache(true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapText);
        OverlayOptions textOverlayOptions = new MarkerOptions().icon(bitmapDescriptor).position(latLng);
        m_baiduMap.addOverlay(textOverlayOptions);
    }

    private void InitView()
    {
        m_context = m_mapView.getContext();
        m_activity = getActivity();
        m_textView = m_mapView.findViewById(R.id.textView_baidu);
        m_baiduMapView = m_mapView.findViewById(R.id.mapView_baidu);
        m_infoWindow = (LinearLayout) LayoutInflater.from(m_activity).inflate(R.layout.map_info_window, null);
        m_infoWindow.setOnClickListener(this::onClick);
        m_btnLocation = m_mapView.findViewById(R.id.btn_location_baidu);
        m_btnLocation.setOnClickListener(this::onClick);
        m_btnTraffic = m_mapView.findViewById(R.id.btn_trafficlight_baidu);
        m_btnTraffic.setOnClickListener(this::onClick);
        m_btnLocus = m_mapView.findViewById(R.id.btn_locus_baidu);
        m_btnLocus.setOnClickListener(this::onClick);
        m_btnTask = m_mapView.findViewById(R.id.btn_task_baidu);
        m_btnTask.setOnClickListener(this::onClick);
        m_btnDefense = m_mapView.findViewById(R.id.btn_defense_baidu);
        m_btnDefense.setOnClickListener(this::onClick);
        //动态获取权限
        RequestPermission();
        //注册SDK广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        //获取传感器管理服务
        m_sensorManager = (SensorManager) m_context.getSystemService(SENSOR_SERVICE);
        m_sdkReceiver = new SDKReceiver();
        m_context.registerReceiver(m_sdkReceiver, iFilter);
        //地图初始化
        m_baiduMap = m_baiduMapView.getMap();
        m_baiduMap.setOnMapClickListener(this);
        m_baiduMap.setOnMarkerClickListener(this::onMarkerClick);
        //地图加载完毕回调
        m_baiduMap.setOnMapLoadedCallback(() -> SetMapStateAndMarkOptions());
        m_btnMonitorTarget = m_mapView.findViewById(R.id.btn_monitor_target);
        m_btnMonitorTarget.setOnClickListener(this::onClick);
        if (null != m_deviceInfo)
        {
            m_btnMonitorTarget.setText("监控目标:" + m_deviceInfo.getM_name());
        }
    }

    /**
     * 根据地理位置查找经纬度
     *
     * @param geoCodeResult
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult)
    {
        if (null == geoCodeResult || SearchResult.ERRORNO.NO_ERROR != geoCodeResult.error)
        {
            Toast.makeText(m_context, "未能找到结果", Toast.LENGTH_SHORT);
        }
    }

    /**
     * 根据经纬度查找地理位置
     *
     * @param reverseGeoCodeResult
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult)
    {
        if (null == reverseGeoCodeResult || SearchResult.ERRORNO.NO_ERROR != reverseGeoCodeResult.error)
        {
            Toast.makeText(m_context, "未能找到结果", Toast.LENGTH_SHORT);
        }
        else
        {
            m_latLngStr = reverseGeoCodeResult.getAddress();
            //DrawMarkerText(m_latLng, m_latLngStr, Color.RED, 0xAAFFFF80);
        }
    }

    @Override
    public void onDestroy()
    {
        //因为采用显示隐藏的方式来切换,资源暂时不能销毁
        //        if(m_marker != null)
        //        {
        //            m_marker.cancelAnimation();
        //            m_marker.remove();
        //        }
        //        //MapView的生命周期与Fragment同步,当Fragment销毁时需调用MapView.destroy()
        //        m_baiduMapView.onDestroy();
        super.onDestroy();
        //        //回收Bitmap资源
        //        ICON_MARKER.recycle();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        double x = event.values[SensorManager.DATA_X];
        if (Math.abs(x - m_lastX) > 1.0)
        {
            m_currentDirection = (int) x;
            m_locationData = new MyLocationData.Builder().accuracy(m_currentAccracy).direction(m_currentDirection).latitude(m_currentLat).longitude(m_currentLon).build();
            //此处设置开发者获取到的方向信息,顺时针0-360
            m_baiduMap.setMyLocationData(m_locationData);
        }
        m_lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
        //取消注册传感器监听
        m_sensorManager.unregisterListener(this);
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
        //地理编码
        m_geoCoder = GeoCoder.newInstance();
        m_geoCoder.setOnGetGeoCodeResultListener(this);
        if (getArguments() != null)
        {
            //获取设备信息
            m_deviceInfo = getArguments().getParcelable("DEVICEINFO");
            if (null != m_deviceInfo)
            {
                m_latLng = new LatLng(m_deviceInfo.getM_lat(), m_deviceInfo.getM_lot());
                //设置反地理编码坐标
                m_geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(m_latLng).newVersion(1).radius(500));
            }
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
        m_mapView = inflater.inflate(R.layout.fragment_map_map, container, false);
        InitView();
        return m_mapView;
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
        switch (v.getId())
        {
            case R.id.btn_monitor_target:
                DeviceChooseFragment deviceChooseFragment = DeviceChooseFragment.newInstance(m_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, deviceChooseFragment, "deviceChooseFragment").commitNow();
                break;
            case R.id.btn_location_baidu:
                if (null != m_deviceInfo)
                {
                    MapStatus mapStatus = new MapStatus.Builder().target(m_latLng).zoom(16).build();
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    m_baiduMap.setMapStatus(mapStatusUpdate);
                }
                break;
            case R.id.btn_trafficlight_baidu:
                if (!m_trafficEnabled)
                {
                    m_baiduMap.setTrafficEnabled(true);
                    m_trafficEnabled = true;
                }
                else
                {
                    m_baiduMap.setTrafficEnabled(false);
                    m_trafficEnabled = false;
                }
                break;
            case R.id.btn_locus_baidu:
                if (null != m_deviceInfo)
                {
                    TrackQueryFragment trackQueryFragment = TrackQueryFragment.newInstance(m_deviceInfo);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, trackQueryFragment, "trackQueryFragment").commitNow();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("请选择设备");
                    builder.show();
                }
                break;
            case R.id.btn_task_baidu:
                break;
            case R.id.btn_defense_baidu:
                break;
        }
    }

    /**
     * 恢复Fragment时被回调,onStart()执行之后一定会执行onResume()方法,该方法执行之后才能交互
     */
    @Override
    public void onResume()
    {
        super.onResume();
        //为系统的方向传感器注册监听器
        m_sensorManager.registerListener(this, m_sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

}
