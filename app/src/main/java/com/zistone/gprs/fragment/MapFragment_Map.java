package com.zistone.gprs.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.animation.Animation;
import com.baidu.mapapi.animation.Transformation;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.zistone.gprs.R;
import com.zistone.gprs.dialog.CreateFenceDialog;
import com.zistone.gprs.dialog.DefenseDialog;
import com.zistone.gprs.dialog.DeviceInfoDialog;
import com.zistone.gprs.dialog.FenceInfoDialog;
import com.zistone.gprs.pojo.FenceInfo;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.gprs.pojo.LocationInfo;
import com.zistone.gprs.util.PropertiesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.SENSOR_SERVICE;

public class MapFragment_Map extends Fragment implements BaiduMap.OnMapClickListener, View.OnClickListener, SensorEventListener, OnGetGeoCoderResultListener, Serializable, BaiduMap.OnMarkerClickListener, BaiduMap.OnMapLoadedCallback
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String TAG = "MapFragment_Map";
    private static final String MARKERID_ICON = "MARKER";
    private static final BitmapDescriptor ICON_MARKER1 = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark1);
    private static final BitmapDescriptor ICON_MARKER2 = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark2);
    private static final BitmapDescriptor ICON_MARKER3 = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark3);
    private static final int MESSAGE_QUERYLOCATION_RREQUEST_FAIL = 1;
    private static final int MESSAGE_QUERYLOCATION_RESPONSE_FAIL = 2;
    private static final int MESSAGE_QUERYLOCATION_RESPONSE_SUCCESS = 3;
    private static final int MESSAGE_FENCE_ADD_RREQUEST_FAIL = 4;
    private static final int MESSAGE_FENCE_ADD_RESPONSE_FAIL = 5;
    private static final int MESSAGE_FENCE_ADD_RESPONSE_SUCCESS = 6;
    private static final int MESSAGE_FENCE_DEL_RREQUEST_FAIL = 7;
    private static final int MESSAGE_FENCE_DEL_RESPONSE_FAIL = 8;
    private static final int MESSAGE_FENCE_DEL_RESPONSE_SUCCESS = 9;
    private static final int MESSAGE_FENCE_QUERY_RREQUEST_FAIL = 13;
    private static final int MESSAGE_FENCE_QUERY_RESPONSE_FAIL = 14;
    private static final int MESSAGE_FENCE_QUERY_RESPONSE_SUCCESS = 15;
    private static String URL_LOCATION_LASTDAYS;
    private static String URL_FENCE_ADD;
    private static String URL_FENCE_DEL;
    private static String URL_FENCE_UPDATE;
    private static String URL_FENCE_QUERY;
    private Context _context;
    private MyLocationListener _locationListener = new MyLocationListener();
    private View _mapView;
    private TextView _txtView;
    private MapView _baiduMapView;
    private BaiduMap _baiduMap;
    private SensorManager _sensorManager;
    //是否首次定位
    private boolean _isFirstLoc = true;
    private double _lastX;
    private MyLocationData _locationData;
    private int _currentDirection;
    //当前纬度
    private double _currentLat;
    //当前经度
    private double _currentLon;
    //当前定位精度
    private float _currentAccracy;
    private boolean _isPermissionRequested;
    private SDKReceiver _sdkReceiver;
    //设备标记
    private Marker _marker;
    //经纬度对应的地址信息
    private String _latLngStr = "";
    //地理编码搜索
    private GeoCoder _geoCoder;
    //设备的经纬度
    private LatLng _latLng;
    //设备信息
    private DeviceInfo _deviceInfo;
    private Activity _activity;
    private OnFragmentInteractionListener _listener;
    private ImageButton _btnLocation;
    private ImageButton _btnUpLocation;
    private ImageButton _btnDownLocation;
    private ImageButton _btnTraffic;
    private ImageButton _btnLocus;
    private ImageButton _btnTask;
    private ImageButton _btnDefense;
    private Button _btnMonitorTarget;
    //是否显示城市热力图
    private boolean _trafficEnabled = false;
    //圆形围栏中心点坐标
    private LatLng _circleCenter;
    private ImageButton _btn_up;
    private ImageButton _btn_down;
    //创建围栏对话框
    private CreateFenceDialog _createFenceDialog;
    private CreateFenceDialog.Callback _createFenceCallback;
    //设备信息对话框
    private DeviceInfoDialog _deviceInfoDialog;
    private DeviceInfoDialog.Callback _deviceInfoCallback;
    //围栏信息对话框
    private FenceInfoDialog _fenceInfoDialog;
    private FenceInfoDialog.Callback _fenceInfoCallback;
    //区域设防对话框
    private DefenseDialog _defenseDialog;
    private DefenseDialog.Callback _defenseCallback;
    //该设备对应的围栏
    private List<FenceInfo> _fenceInfoList = new ArrayList<>();
    private List<LocationInfo> _locationNowMonthEverDayList = new ArrayList<>();
    private int _nowMonthHistoryLocationIndex = 0;
    private Map<String, Overlay> _overlayMap = new HashMap<>();
    private boolean _isShowCreateFenceDialog = false;
    private boolean _isClickDeviceMark = false;

    public static MapFragment_Map newInstance(DeviceInfo deviceInfo)
    {
        MapFragment_Map fragment = new MapFragment_Map();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public void onButtonPressed(Uri uri)
    {
        if(_listener != null)
        {
            _listener.onFragmentInteraction(uri);
        }
    }

    private void InitListener()
    {
        _createFenceCallback = new CreateFenceDialog.Callback()
        {
            @Override
            public void onSureCallback(String name, String address, double radius)
            {
                FenceInfo fenceInfo = new FenceInfo();
                fenceInfo.setDeviceId(_deviceInfo.getDeviceId());
                fenceInfo.setName(name);
                fenceInfo.setAddress(address);
                fenceInfo.setRadius(radius);
                fenceInfo.setSetTime(new Date());
                fenceInfo.setLat(_circleCenter.latitude);
                fenceInfo.setLot(_circleCenter.longitude);
                //围栏在服务端添加成功才在地图上显示
                FenceUtil("add", fenceInfo);
            }

            @Override
            public void onCancelCallback()
            {
            }
        };

        _deviceInfoCallback = new DeviceInfoDialog.Callback()
        {
            @Override
            public void onSetCallback()
            {
                MapFragment_Setting mapFragment_setting = MapFragment_Setting.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_setting, "mapFragment_setting").commitNow();
            }
        };

        _defenseCallback = new DefenseDialog.Callback()
        {
            @Override
            public void onSetCallback()
            {
                Toast.makeText(_context, "请点击屏幕选择设防区域", Toast.LENGTH_LONG).show();
                _isShowCreateFenceDialog = true;
            }

            @Override
            public void onWarnCallback()
            {
            }
        };

        _fenceInfoCallback = new FenceInfoDialog.Callback()
        {
            @Override
            public void onDelCallback()
            {

            }
        };
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            String result = (String) message.obj;
            boolean isNull = result == null || "".equals(result) || "[]".equals(result);
            switch(message.what)
            {
                case MESSAGE_QUERYLOCATION_RREQUEST_FAIL:
                case MESSAGE_FENCE_ADD_RREQUEST_FAIL:
                case MESSAGE_FENCE_DEL_RREQUEST_FAIL:
                case MESSAGE_FENCE_QUERY_RREQUEST_FAIL:
                    Toast.makeText(_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_QUERYLOCATION_RESPONSE_FAIL:
                    Toast.makeText(_context, "服务端历史位置的数据异常", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FENCE_ADD_RESPONSE_FAIL:
                    Toast.makeText(_context, "添加围栏失败", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FENCE_DEL_RESPONSE_FAIL:
                    Toast.makeText(_context, "删除围栏失败", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_FENCE_QUERY_RESPONSE_FAIL:
                {
                    Toast.makeText(_context, "服务端围栏数据异常", Toast.LENGTH_SHORT).show();
                    break;
                }
                //围栏添加成功
                case MESSAGE_FENCE_ADD_RESPONSE_SUCCESS:
                {
                    if(isNull)
                    {
                        return;
                    }
                    FenceInfo fenceInfo = JSON.parseObject(result, FenceInfo.class);
                    MarkerOptions markerOptions = new MarkerOptions().position(_circleCenter).icon(ICON_MARKER3);
                    //标记添加至地图中
                    _baiduMap.addOverlay(markerOptions);
                    //围栏添加至地图中,但不显示
                    OverlayOptions overlayOptions = new CircleOptions().fillColor(0x000000FF).center(new LatLng(fenceInfo.getLat(), fenceInfo.getLot())).stroke(new Stroke(5, Color.rgb(0x23, 0x19, 0xDC))).radius((int) fenceInfo.getRadius());
                    Overlay overlay = _baiduMap.addOverlay(overlayOptions);
                    overlay.setVisible(false);
                    _overlayMap.put(fenceInfo.getId() + "", overlay);
                    _fenceInfoList.add(fenceInfo);
                    break;
                }
                //围栏删除成功
                case MESSAGE_FENCE_DEL_RESPONSE_SUCCESS:
                {
                    if(isNull)
                    {
                        return;
                    }
                    break;
                }
                //查询围栏有返回
                case MESSAGE_FENCE_QUERY_RESPONSE_SUCCESS:
                {
                    if(isNull)
                    {
                        return;
                    }
                    _fenceInfoList = JSON.parseArray(result, FenceInfo.class);
                    for(FenceInfo temp : _fenceInfoList)
                    {
                        LatLng latLng = new LatLng(temp.getLat(), temp.getLot());
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(ICON_MARKER3);
                        //标记添加至地图中
                        _baiduMap.addOverlay(markerOptions);
                        //围栏添加至地图中,但不显示
                        OverlayOptions overlayOptions = new CircleOptions().fillColor(0x000000FF).center(latLng).stroke(new Stroke(5, Color.rgb(0x23, 0x19, 0xDC))).radius((int) temp.getRadius());
                        Overlay overlay = _baiduMap.addOverlay(overlayOptions);
                        overlay.setVisible(false);
                        _overlayMap.put(temp.getId() + "", overlay);
                    }
                    break;
                }
                //查询历史位置有返回
                case MESSAGE_QUERYLOCATION_RESPONSE_SUCCESS:
                {
                    if(isNull)
                    {
                        return;
                    }
                    _locationNowMonthEverDayList = JSON.parseArray(result, LocationInfo.class);
                    for(LocationInfo tempLocationInfo : _locationNowMonthEverDayList)
                    {
                        LatLng latLng = new LatLng(tempLocationInfo.getLat(), tempLocationInfo.getLot());
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).icon(ICON_MARKER1);
                        //标记添加至地图中
                        _baiduMap.addOverlay(markerOptions);
                    }
                    break;
                }
            }
        }
    };

    private void QueryLastDays(int days) throws ParseException
    {
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            //RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("deviceId", _deviceInfo.getDeviceId());
            builder.add("days", days + "");
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder().post(requestBody).url(URL_LOCATION_LASTDAYS).build();
            Call call = okHttpClient.newCall(request);
            //异步请求
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, String.format("查询设备%s最后%s天最后位置失败:%s", _deviceInfo.getDeviceId(), days, e.toString()));
                    Message message = handler.obtainMessage(MESSAGE_QUERYLOCATION_RREQUEST_FAIL, "请求失败:" + e.toString());
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    String responseStr = response.body().string();
                    Log.i(TAG, String.format("查询设备%s最后%s天最后位置的响应内容:%s", _deviceInfo.getDeviceId(), days, responseStr));
                    if(response.isSuccessful())
                    {
                        Message message = handler.obtainMessage(MESSAGE_QUERYLOCATION_RESPONSE_SUCCESS, responseStr);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Message message = handler.obtainMessage(MESSAGE_QUERYLOCATION_RESPONSE_FAIL, responseStr);
                        handler.sendMessage(message);
                    }
                }
            });
            Looper.loop();
        }).start();
    }

    /**
     * 地图加载成功后
     */
    @Override
    public void onMapLoaded()
    {
        if(null == _latLng)
        {
            return;
        }
        //设置标记的位置和图标
        MarkerOptions markerOptions = new MarkerOptions().position(_latLng).icon(ICON_MARKER2);
        //标记添加至地图中
        _marker = (Marker) (_baiduMap.addOverlay(markerOptions));
        //定义地图缩放级别3~16,值越大地图越精细
        MapStatus mapStatus = new MapStatus.Builder().target(_latLng).zoom(16).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        //改变地图状态
        _baiduMap.setMapStatus(mapStatusUpdate);
        //添加平移动画
        //        _marker.setAnimation(Transformation());
        //        _marker.startAnimation();
        try
        {
            QueryLastDays(7);
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker)
    {
        LatLng latLng = marker.getPosition();
        _isClickDeviceMark = true;
        for(FenceInfo temp : _fenceInfoList)
        {
            //点击的图标位置与围栏的位置相同则表示为围栏的图标
            if(temp.getLat() == latLng.latitude && temp.getLot() == latLng.longitude)
            {
                if(_overlayMap.containsKey(temp.getId() + ""))
                {
                    _overlayMap.get(temp.getId() + "").setVisible(true);
                    _isClickDeviceMark = false;
                    if(_fenceInfoDialog == null)
                    {
                        _fenceInfoDialog = new FenceInfoDialog(_activity, _fenceInfoCallback, temp);
                    }
                    _fenceInfoDialog.setCanceledOnTouchOutside(true);
                    _fenceInfoDialog.show();
                }
            }
            else
            {
                _overlayMap.get(temp.getId() + "").setVisible(false);
            }
        }
        //经纬度->地址,查看设备信息和围栏信息都需要带入地址,所以查看设备信息的逻辑写在了onGetReverseGeoCodeResult()转换里
        _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).newVersion(1).radius(500));
        return false;
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        //隐藏围栏
        Iterator<Map.Entry<String, Overlay>> iterator = _overlayMap.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<String, Overlay> entry = iterator.next();
            entry.getValue().setVisible(false);
        }
        _circleCenter = latLng;
        //经纬度->地址,创建围栏时需要带入地址,所以逻辑写在了onGetReverseGeoCodeResult()转换里
        _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).newVersion(1).radius(500));
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
        Point point = _baiduMap.getProjection().toScreenLocation(_latLng);
        LatLng latLng = _baiduMap.getProjection().fromScreenLocation(new Point(point.x, point.y - 30));
        Transformation mTransforma = new Transformation(_latLng, latLng, _latLng);
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
            if(null == _baiduMapView || null == location || BDLocation.TypeServerError == location.getLocType())
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
            _currentLat = latitude;
            sb.append("\n纬度 : ");
            sb.append(latitude);
            //获取经度信息
            double longitude = location.getLongitude();
            _currentLon = longitude;
            sb.append("\n经度 : ");
            sb.append(longitude);
            //获取定位精度,默认值为0.0f
            float radius = location.getRadius();
            _currentAccracy = radius;
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
                    Log.i(TAG, String.format(">>>%s\t%s\t%s", poi.getId(), poi.getName(), poi.getRank()));
                }
            }
            Log.i(TAG, ">>>" + sb.toString());
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
                sb.append("\n网络定位成功");
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
            //将定位信息显示在TextView
            //PrintLocationResult(sb.toString());
            _locationData = new MyLocationData.Builder().accuracy(location.getRadius()).direction(_currentDirection).latitude(location.getLatitude()).longitude(location.getLongitude()).build();
            //此处设置开发者获取到的方向信息,顺时针0-360
            _baiduMap.setMyLocationData(_locationData);
            //如果是首次定位
            if(_isFirstLoc)
            {
                _isFirstLoc = false;
                //根据经纬度定位位置
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                _baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
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
            if(TextUtils.isEmpty(action))
            {
                return;
            }
            //鉴权错误信息描述
            _txtView.setTextColor(Color.RED);
            if(action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR))
            {
                _txtView.setText("Key验证出错!错误码:" + intent.getIntExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0) + ";错误信息:" + intent.getStringExtra(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_MESSAGE));
                _txtView.setTextColor(Color.RED);
                _txtView.setVisibility(View.INVISIBLE);
            }
            else if(action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR))
            {
                _txtView.setText("网络出错");
                _txtView.setTextColor(Color.RED);
                _txtView.setVisibility(View.INVISIBLE);
            }
            else if(action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK))
            {
                _txtView.setText("Key验证成功!功能可以正常使用");
                _txtView.setTextColor(Color.GREEN);
                _txtView.setVisibility(View.GONE);
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
        if(Build.VERSION.SDK_INT >= 23 && !_isPermissionRequested)
        {
            _isPermissionRequested = true;
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
            for(String perm : permissions)
            {
                if(PackageManager.PERMISSION_GRANTED != _context.checkSelfPermission(perm))
                {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            if(!permissionsList.isEmpty())
            {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }

    /**
     * 查询所有电子围栏
     */
    private void FenceUtil(String param, FenceInfo fenceInfo)
    {
        if(_deviceInfo == null)
        {
            return;
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
        new Thread(() ->
        {
            Looper.prepare();
            switch(param)
            {
                case "add":
                {
                    if(fenceInfo != null)
                    {
                        fenceInfo.setDeviceId(_deviceInfo.getDeviceId());
                        String jsonData = JSON.toJSONString(fenceInfo);
                        RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
                        Request request = new Request.Builder().post(requestBody).url(URL_FENCE_ADD).build();
                        Call call = okHttpClient.newCall(request);
                        //异步请求
                        call.enqueue(new Callback()
                        {
                            @Override
                            public void onFailure(@NotNull Call call, @NotNull IOException e)
                            {
                                Log.e(TAG, "添加围栏失败:" + e.toString());
                                Message message = handler.obtainMessage(MESSAGE_FENCE_ADD_RREQUEST_FAIL, "请求失败:" + e.toString());
                                handler.sendMessage(message);
                            }

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                            {
                                String responseStr = response.body().string();
                                if(response.isSuccessful())
                                {
                                    Log.i(TAG, "添加围栏成功:" + responseStr);
                                    Message message = handler.obtainMessage(MESSAGE_FENCE_ADD_RESPONSE_SUCCESS, responseStr);
                                    handler.sendMessage(message);
                                }
                                else
                                {
                                    Log.e(TAG, "添加围栏失败:" + responseStr);
                                    Message message = handler.obtainMessage(MESSAGE_FENCE_ADD_RESPONSE_FAIL, responseStr);
                                    handler.sendMessage(message);
                                }
                            }
                        });
                    }
                    break;
                }
                case "del":
                {
                    FormBody.Builder builder = new FormBody.Builder();
                    builder.add("deviceId", _deviceInfo.getDeviceId());
                    RequestBody requestBody = builder.build();
                    Request request = new Request.Builder().post(requestBody).url(URL_FENCE_DEL).build();
                    Call call = okHttpClient.newCall(request);
                    //异步请求
                    call.enqueue(new Callback()
                    {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e)
                        {
                            Log.e(TAG, "删除围栏失败:" + e.toString());
                            Message message = handler.obtainMessage(MESSAGE_FENCE_DEL_RREQUEST_FAIL, "请求失败:" + e.toString());
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                        {
                            String responseStr = response.body().string();
                            if(response.isSuccessful())
                            {
                                Log.i(TAG, "删除围栏成功:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_FENCE_DEL_RESPONSE_SUCCESS, responseStr);
                                handler.sendMessage(message);
                            }
                            else
                            {
                                Log.e(TAG, "删除围栏失败:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_FENCE_DEL_RESPONSE_FAIL, responseStr);
                                handler.sendMessage(message);
                            }
                        }
                    });
                    break;
                }
                case "update":
                {
                    break;
                }
                case "query":
                {
                    FormBody.Builder builder = new FormBody.Builder();
                    builder.add("deviceId", _deviceInfo.getDeviceId());
                    RequestBody requestBody = builder.build();
                    Request request = new Request.Builder().post(requestBody).url(URL_FENCE_QUERY).build();
                    Call call = okHttpClient.newCall(request);
                    //异步请求
                    call.enqueue(new Callback()
                    {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e)
                        {
                            Log.e(TAG, "查询围栏失败:" + e.toString());
                            Message message = handler.obtainMessage(MESSAGE_FENCE_QUERY_RREQUEST_FAIL, "请求失败:" + e.toString());
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                        {
                            String responseStr = response.body().string();
                            if(response.isSuccessful())
                            {
                                Log.i(TAG, "查询围栏成功:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_FENCE_QUERY_RESPONSE_SUCCESS, responseStr);
                                handler.sendMessage(message);
                            }
                            else
                            {
                                Log.e(TAG, "查询围栏失败:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_FENCE_QUERY_RESPONSE_FAIL, responseStr);
                                handler.sendMessage(message);
                            }
                        }
                    });
                    break;
                }
            }
            Looper.loop();
        }).start();
    }

    /**
     * 根据地理位置查找经纬度
     *
     * @param geoCodeResult
     */
    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult)
    {
        if(null == geoCodeResult || SearchResult.ERRORNO.NO_ERROR != geoCodeResult.error)
        {
            Toast.makeText(_context, "经纬度查询异常", Toast.LENGTH_SHORT).show();
        }
        else
        {
        }
    }

    /**
     * 根据经纬度查找地理位置
     * <p>
     * 创建围栏时需要带入地址信息,所以创建围栏的逻辑放在这里面
     *
     * @param reverseGeoCodeResult
     */
    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult)
    {
        if(null == reverseGeoCodeResult || SearchResult.ERRORNO.NO_ERROR != reverseGeoCodeResult.error)
        {
            Toast.makeText(_context, "地理位置查询异常", Toast.LENGTH_SHORT).show();
        }
        else
        {
            _latLngStr = reverseGeoCodeResult.getAddress();
            //DrawMarkerText(_latLng, _latLngStr, Color.RED, 0xAAFFFF80);
            //设备信息
            if(_isClickDeviceMark)
            {
                if(_deviceInfo != null && !_deviceInfo.getDeviceId().equals(""))
                {
                    _deviceInfoDialog = new DeviceInfoDialog(_activity, _deviceInfoCallback, _deviceInfo, _latLngStr);
                    _deviceInfoDialog.setCanceledOnTouchOutside(true);
                    _deviceInfoDialog.show();
                    _isClickDeviceMark = false;
                }
            }
            //创建围栏
            if(_isShowCreateFenceDialog)
            {
                if(_deviceInfo != null && !_deviceInfo.getDeviceId().equals(""))
                {
                    _createFenceDialog = new CreateFenceDialog(_activity, _createFenceCallback, _latLngStr);
                    _createFenceDialog.setCanceledOnTouchOutside(true);
                    _createFenceDialog.show();
                    _isShowCreateFenceDialog = false;
                }
                else
                {
                    Toast.makeText(_context, "区域设防失败,该设备缺少设备编号", Toast.LENGTH_SHORT);
                }
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        _circleCenter = null;
        //        if(null != ICON_MARKER1)
        //        {
        //            ICON_MARKER1.recycle();
        //        }
        //        if(null != ICON_MARKER2)
        //        {
        //            ICON_MARKER2.recycle();
        //        }
        //        if(null != ICON_MARKER3)
        //        {
        //            ICON_MARKER3.recycle();
        //        }
        if(null != _baiduMap)
        {
            _baiduMap.clear();
            _baiduMap = null;
        }
        //MapView的生命周期与Fragment同步,当Fragment销毁时需调用MapView.destroy()
        if(null != _baiduMapView)
        {
            _baiduMapView.onDestroy();
            _baiduMapView = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        double x = event.values[SensorManager.DATA_X];
        if(Math.abs(x - _lastX) > 1.0)
        {
            _currentDirection = (int) x;
            _locationData = new MyLocationData.Builder().accuracy(_currentAccracy).direction(_currentDirection).latitude(_currentLat).longitude(_currentLon).build();
            //此处设置开发者获取到的方向信息,顺时针0-360
            _baiduMap.setMyLocationData(_locationData);
        }
        _lastX = x;
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
        _sensorManager.unregisterListener(this);
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
        _geoCoder = GeoCoder.newInstance();
        _geoCoder.setOnGetGeoCodeResultListener(this);
        if(getArguments() != null)
        {
            //获取设备信息
            _deviceInfo = getArguments().getParcelable("DEVICEINFO");
            if(null != _deviceInfo)
            {
                _latLng = new LatLng(_deviceInfo.getLat(), _deviceInfo.getLot());
                //经纬度->地址
                _geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(_latLng).newVersion(1).radius(500));
            }
        }
        //获取该设备所有区域设防的位置(电子围栏)
        FenceUtil("query", null);
        InitListener();
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
        _mapView = inflater.inflate(R.layout.fragment_map_map, container, false);
        _context = _mapView.getContext();
        URL_LOCATION_LASTDAYS = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/LocationInfo/FindDescDaysLastDataByDeviceId";
        URL_FENCE_ADD = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/FenceInfo/InsertByDeviceId";
        URL_FENCE_DEL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/FenceInfo/DelById";
        URL_FENCE_UPDATE = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/FenceInfo/UpdateById";
        URL_FENCE_QUERY = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/FenceInfo/FindByDeviceId";
        _activity = getActivity();
        _txtView = _mapView.findViewById(R.id.textView_baidu);
        _baiduMapView = _mapView.findViewById(R.id.mapView_baidu);
        _btnLocation = _mapView.findViewById(R.id.btn_location_baidu);
        _btnLocation.setOnClickListener(this::onClick);
        _btnUpLocation = _mapView.findViewById(R.id.btn_up_baidu);
        _btnUpLocation.setOnClickListener(this::onClick);
        _btnDownLocation = _mapView.findViewById(R.id.btn_down_baidu);
        _btnDownLocation.setOnClickListener(this::onClick);
        _btnTraffic = _mapView.findViewById(R.id.btn_trafficlight_baidu);
        _btnTraffic.setOnClickListener(this::onClick);
        _btnLocus = _mapView.findViewById(R.id.btn_locus_baidu);
        _btnLocus.setOnClickListener(this::onClick);
        _btnTask = _mapView.findViewById(R.id.btn_task_baidu);
        _btnTask.setOnClickListener(this::onClick);
        _btnDefense = _mapView.findViewById(R.id.btn_defense_baidu);
        _btnDefense.setOnClickListener(this::onClick);
        _btn_up = _mapView.findViewById(R.id.btn_up_baidu);
        _btn_up.setOnClickListener(this::onClick);
        _btn_down = _mapView.findViewById(R.id.btn_down_baidu);
        _btn_down.setOnClickListener(this::onClick);
        //动态获取权限
        RequestPermission();
        //注册SDK广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        //获取传感器管理服务
        _sensorManager = (SensorManager) _context.getSystemService(SENSOR_SERVICE);
        _sdkReceiver = new SDKReceiver();
        _context.registerReceiver(_sdkReceiver, iFilter);
        //地图初始化
        _baiduMap = _baiduMapView.getMap();
        _baiduMap.setOnMapClickListener(this);
        _baiduMap.setOnMarkerClickListener(this::onMarkerClick);
        //地图加载完毕回调
        _baiduMap.setOnMapLoadedCallback(this::onMapLoaded);
        _btnMonitorTarget = _mapView.findViewById(R.id.btn_monitor_target);
        _btnMonitorTarget.setOnClickListener(this::onClick);
        if(null != _deviceInfo)
        {
            _btnMonitorTarget.setText("监控目标:" + _deviceInfo.getName());
        }
        return _mapView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            _listener = (OnFragmentInteractionListener) context;
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
        _listener = null;
    }

    @Override
    public void onClick(View v)
    {
        int historyLocationTotal = _locationNowMonthEverDayList.size();
        switch(v.getId())
        {
            case R.id.btn_monitor_target:
                MapFragment_Choose mapFragment_choose = MapFragment_Choose.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_choose, "mapFragment_choose").commitNow();
                break;
            case R.id.btn_location_baidu:
                if(null != _deviceInfo)
                {
                    MapStatus mapStatus = new MapStatus.Builder().target(_latLng).zoom(16).build();
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    _baiduMap.setMapStatus(mapStatusUpdate);
                }
                break;
            case R.id.btn_up_baidu:
                if(_nowMonthHistoryLocationIndex < historyLocationTotal)
                {
                    LocationInfo locationInfo = _locationNowMonthEverDayList.get(_nowMonthHistoryLocationIndex);
                    LatLng latLng = new LatLng(locationInfo.getLat(), locationInfo.getLot());
                    MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(16).build();
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    _baiduMap.setMapStatus(mapStatusUpdate);
                    _nowMonthHistoryLocationIndex++;
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
                    builder.setPositiveButton("知道了", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("已到起始日");
                    builder.show();
                }
                break;
            case R.id.btn_down_baidu:
                if(_nowMonthHistoryLocationIndex > 0)
                {
                    _nowMonthHistoryLocationIndex--;
                    LocationInfo locationInfo = _locationNowMonthEverDayList.get(_nowMonthHistoryLocationIndex);
                    LatLng latLng = new LatLng(locationInfo.getLat(), locationInfo.getLot());
                    MapStatus mapStatus = new MapStatus.Builder().target(latLng).zoom(16).build();
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                    _baiduMap.setMapStatus(mapStatusUpdate);
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
                    builder.setPositiveButton("知道了", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("已到截止日");
                    builder.show();
                }
                break;
            case R.id.btn_trafficlight_baidu:
                if(!_trafficEnabled)
                {
                    _baiduMap.setTrafficEnabled(true);
                    _trafficEnabled = true;
                }
                else
                {
                    _baiduMap.setTrafficEnabled(false);
                    _trafficEnabled = false;
                }
                break;
            case R.id.btn_locus_baidu:
                if(null != _deviceInfo)
                {
                    MapFragment_TrackQuery mapFragment_trackQuery = MapFragment_TrackQuery.newInstance(_deviceInfo);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_trackQuery, "mapFragment_trackQuery").commitNow();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
                    builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("请选择设备");
                    builder.show();
                }
                break;
            case R.id.btn_task_baidu:
                if(_deviceInfo != null)
                {
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
                    builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("请选择设备");
                    builder.show();
                }
                break;
            case R.id.btn_defense_baidu:
                if(_deviceInfo != null)
                {
                    if(_defenseDialog == null)
                    {
                        _defenseDialog = new DefenseDialog(_activity, _defenseCallback);
                    }
                    _defenseDialog.setCanceledOnTouchOutside(true);
                    _defenseDialog.show();
                }
                else
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(_activity);
                    builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
                    builder.setMessage("请选择设备");
                    builder.show();
                }
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
        _sensorManager.registerListener(this, _sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
    }

}
