package com.zistone.blowdown_app.entity;

import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.model.LatLng;
import com.zistone.blowdown_app.R;

/**
 * 每个电子围栏的Marker点坐标、图标、所以围栏标识
 */
public class FenceItem implements ClusterItem
{
    private final LatLng m_position;
    private BitmapDescriptor m_defenseMark = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark3);
    private String m_key;

    public FenceItem(String key, LatLng latLng)
    {
        m_key = key;
        m_position = latLng;
    }

    @Override
    public LatLng getPosition()
    {
        return m_position;
    }

    @Override
    public String getKey()
    {
        return m_key;
    }

    @Override
    public BitmapDescriptor getBitmapDescriptor()
    {
        return m_defenseMark;
    }

    @Override
    public boolean equals(Object o)
    {
        if(this == o)
        {
            return true;
        }
        if(o == null || getClass() != o.getClass())
        {
            return false;
        }
        FenceItem fenceItem = (FenceItem) o;
        return m_key.equals(fenceItem.m_key);
    }

    @Override
    public int hashCode()
    {
        return m_key.hashCode();
    }

    @Override
    public String toString()
    {
        return "FenceItem{Key='" + m_key + '\'' + ", Position=" + m_position + '}';
    }
}
