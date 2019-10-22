package com.zistone.blowdown_app.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationInfo
{
    /**
     * 自增主键(由数据库生成)
     */
    private String m_id;

    /**
     * 设备编号
     */
    private String m_deviceId;

    /**
     * 纬度
     */
    private double m_lat;

    /**
     * 经度
     */
    private double m_lot;

    /**
     * 定位时间
     */
    private Date m_createTime;

    @Override
    public String toString()
    {
        return "LocationInfo{" + "m_id='" + m_id + '\'' + ", m_deviceId='" + m_deviceId + '\'' + ", m_lat=" + m_lat + ", m_lot=" + m_lot + ", m_createTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_createTime) + '}';
    }

    public String getM_id()
    {
        return m_id;
    }

    public void setM_id(String m_id)
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

    public Date getM_createTime()
    {
        return m_createTime;
    }

    public void setM_createTime(Date m_createTime)
    {
        this.m_createTime = m_createTime;
    }
}
