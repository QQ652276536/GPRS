package com.zistone.gprs.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.zistone.gprs.R;

import java.util.List;

public class MapUtil
{
    private static MapUtil INSTANCE = new MapUtil();

    private MapStatus mapStatus = null;

    private Marker mMoveMarker = null;

    public MapView mapView = null;

    public BaiduMap baiduMap = null;

    public LatLng lastPoint = null;

    /**
     * 路线覆盖物
     */
    public Overlay polylineOverlay = null;

    private MapUtil()
    {
    }

    public static MapUtil getInstance()
    {
        return INSTANCE;
    }

    public void init(MapView view)
    {
        mapView = view;
        baiduMap = mapView.getMap();
        mapView.showZoomControls(false);
    }

    public void onPause()
    {
        if(null != mapView)
        {
            mapView.onPause();
        }
    }

    public void onResume()
    {
        if(null != mapView)
        {
            mapView.onResume();
        }
    }

    public void clear()
    {
        lastPoint = null;
        if(null != mMoveMarker)
        {
            mMoveMarker.remove();
            mMoveMarker = null;
        }
        if(null != polylineOverlay)
        {
            polylineOverlay.remove();
            polylineOverlay = null;
        }
        if(null != baiduMap)
        {
            baiduMap.clear();
            baiduMap = null;
        }
        mapStatus = null;
        if(null != mapView)
        {
            mapView.onDestroy();
            mapView = null;
        }
    }

    /**
     * 更新地图状态
     *
     * @param currentPoint
     * @param showMarker
     * @param context
     */
    public void UpdateStatus(LatLng currentPoint, boolean showMarker, Context context)
    {
        if(null == baiduMap || null == currentPoint)
        {
            return;
        }

        if(null != baiduMap.getProjection())
        {
            //获取屏幕尺寸
            int screenHeight = getMetrics(context).heightPixels;
            int screenWidth = getMetrics(context).widthPixels;
            Point screenPoint = baiduMap.getProjection().toScreenLocation(currentPoint);
            // 点在屏幕上的坐标超过限制范围，则重新聚焦底图
            if(screenPoint.y < 200 || screenPoint.y > screenHeight - 500 || screenPoint.x < 200 || screenPoint.x > screenWidth - 200 || null == mapStatus)
            {
                animateMapStatus(currentPoint, 15.0f);
            }
        }
        //第一次定位时，聚焦底图
        else if(null == mapStatus)
        {
            setMapStatus(currentPoint, 15.0f);
        }
        //显示图标
        if(showMarker)
        {
            addMarker(currentPoint);
        }

    }

    /**
     * 获取当前屏幕的尺寸大小
     *
     * @param context
     * @return
     */
    public DisplayMetrics getMetrics(Context context)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }

    public Marker addOverlay(LatLng currentPoint, BitmapDescriptor icon, Bundle bundle)
    {
        OverlayOptions overlayOptions = new MarkerOptions().position(currentPoint).icon(icon).zIndex(9).draggable(true);
        Marker marker = (Marker) baiduMap.addOverlay(overlayOptions);
        if(null != bundle)
        {
            marker.setExtraInfo(bundle);
        }
        return marker;
    }

    /**
     * 添加地图覆盖物
     */
    public void addMarker(LatLng currentPoint)
    {
        if(null == mMoveMarker)
        {
            mMoveMarker = addOverlay(currentPoint, BitmapDescriptorFactory.fromResource(R.drawable.icon_mark), null);
            return;
        }

        if(null != lastPoint)
        {
            moveLooper(currentPoint);
        }
        else
        {
            lastPoint = currentPoint;
            mMoveMarker.setPosition(currentPoint);
        }
    }

    /**
     * 移动逻辑
     */
    public void moveLooper(LatLng endPoint)
    {

        mMoveMarker.setPosition(lastPoint);
        mMoveMarker.setRotate((float) CommonUtil.getAngle(lastPoint, endPoint));

        double slope = CommonUtil.getSlope(lastPoint, endPoint);
        // 是不是正向的标示（向上设为正向）
        boolean isReverse = (lastPoint.latitude > endPoint.latitude);
        double intercept = CommonUtil.getInterception(slope, lastPoint);
        double xMoveDistance = isReverse ? CommonUtil.getXMoveDistance(slope) : -1 * CommonUtil.getXMoveDistance(slope);

        for(double latitude = lastPoint.latitude; latitude > endPoint.latitude == isReverse; latitude = latitude - xMoveDistance)
        {
            LatLng latLng;
            if(slope != Double.MAX_VALUE)
            {
                latLng = new LatLng(latitude, (latitude - intercept) / slope);
            }
            else
            {
                latLng = new LatLng(latitude, lastPoint.longitude);
            }
            mMoveMarker.setPosition(latLng);
        }
    }

    /**
     * 绘制历史轨迹
     */
    public void drawHistoryTrack(List<LatLng> points)
    {
        // 绘制新覆盖物前，清空之前的覆盖物
        baiduMap.clear();
        if(points == null || points.size() == 0)
        {
            if(null != polylineOverlay)
            {
                polylineOverlay.remove();
                polylineOverlay = null;
            }
            return;
        }

        if(points.size() == 1)
        {
            OverlayOptions startOptions = new MarkerOptions().position(points.get(0)).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_mark)).zIndex(9).draggable(true);
            baiduMap.addOverlay(startOptions);
            animateMapStatus(points.get(0), 18.0f);
            return;
        }

        LatLng startPoint;
        LatLng endPoint;
        startPoint = points.get(0);
        endPoint = points.get(points.size() - 1);
        // 添加起点图标
        OverlayOptions startOptions = new MarkerOptions().position(startPoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_start)).zIndex(9).draggable(true);
        // 添加终点图标
        OverlayOptions endOptions = new MarkerOptions().position(endPoint).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_end)).zIndex(9).draggable(true);

        // 添加路线（轨迹）
        OverlayOptions polylineOptions = new PolylineOptions().width(10).color(Color.BLUE).points(points);

        baiduMap.addOverlay(startOptions);
        baiduMap.addOverlay(endOptions);
        polylineOverlay = baiduMap.addOverlay(polylineOptions);

        OverlayOptions markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_point)).position(points.get(points.size() - 1)).rotate((float) CommonUtil.getAngle(points.get(0), points.get(1)));
        mMoveMarker = (Marker) baiduMap.addOverlay(markerOptions);

        animateMapStatus(points);
    }

    public void animateMapStatus(List<LatLng> points)
    {
        if(null == points || points.isEmpty())
        {
            return;
        }
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(LatLng point : points)
        {
            builder.include(point);
        }
        MapStatusUpdate msUpdate = MapStatusUpdateFactory.newLatLngBounds(builder.build());
        baiduMap.animateMapStatus(msUpdate);
    }

    public void animateMapStatus(LatLng point, float zoom)
    {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        baiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    public void setMapStatus(LatLng point, float zoom)
    {
        MapStatus.Builder builder = new MapStatus.Builder();
        mapStatus = builder.target(point).zoom(zoom).build();
        baiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
    }

    public void refresh()
    {
        LatLng mapCenter = baiduMap.getMapStatus().target;
        float mapZoom = baiduMap.getMapStatus().zoom - 1.0f;
        setMapStatus(mapCenter, mapZoom);
    }

}

