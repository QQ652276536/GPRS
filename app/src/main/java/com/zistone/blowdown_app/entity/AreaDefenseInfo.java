package com.zistone.blowdown_app.entity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AreaDefenseInfo
{

    /**
     * 自增主键(由数据库生成)
     */
    private int m_id;

    /**
     * 设备编号,设备自带
     */
    private String m_deviceId;

    /**
     * 区域名称
     */
    private String m_name;

    /**
     * 地址
     */
    private String m_address;

    /**
     * 设置时间
     */
    private Date m_setTime;

    /**
     * 半径
     */
    private double m_radius;

    /**
     * 纬度
     */
    private double m_lat;

    /**
     * 经度
     */
    private double m_lot;

    @Override
    public String toString()
    {
        return "AreaDefenseInfo{" + "m_id=" + m_id + ", m_deviceId='" + m_deviceId + '\'' + ", m_name='" + m_name + '\'' + ", m_address='" + m_address + '\'' + ", m_setTime=" + m_setTime + ", m_radius=" + m_radius + ", m_lat=" + m_lat + ", m_lot=" + m_lot + '}';
    }

    public int getM_id()
    {
        return m_id;
    }

    public void setM_id(int m_id)
    {
        this.m_id = m_id;
    }

    public String getM_deviceId()
    {
        return m_deviceId;
    }

    public void setM_deviceId(String m_deviceId)
    {
        this.m_deviceId = m_deviceId;
    }

    public String getM_name()
    {
        return m_name;
    }

    public void setM_name(String m_name)
    {
        this.m_name = m_name;
    }

    public String getM_address()
    {
        return m_address;
    }

    public void setM_address(String m_address)
    {
        this.m_address = m_address;
    }

    public Date getM_setTime()
    {
        return m_setTime;
    }

    public void setM_setTime(Date m_setTime)
    {
        this.m_setTime = m_setTime;
    }

    public double getM_radius()
    {
        return m_radius;
    }

    public void setM_radius(double m_radius)
    {
        this.m_radius = m_radius;
    }

    public double getM_lat()
    {
        return m_lat;
    }

    public void setM_lat(double m_lat)
    {
        this.m_lat = m_lat;
    }

    public double getM_lot()
    {
        return m_lot;
    }

    public void setM_lot(double m_lot)
    {
        this.m_lot = m_lot;
    }
}