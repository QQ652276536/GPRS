package com.zistone.gprs.pojo;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FenceInfo
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public String toString()
    {
        return "FenceInfo{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", setTime=" + (setTime != null ? SIMPLEDATEFORMAT.format(setTime) : null) +
                ", radius=" + radius +
                ", lat=" + lat +
                ", lot=" + lot +
                '}';
    }

    /**
     * 自增主键(由数据库生成)
     */
    private int id;

    /**
     * 设备编号,设备自带
     */
    private String deviceId;

    /**
     * 区域名称
     */
    private String name;

    /**
     * 地址
     */
    private String address;

    /**
     * 设置时间
     */
    private Date setTime;

    /**
     * 半径
     */
    private double radius;

    /**
     * 纬度
     */
    private double lat;

    /**
     * 经度
     */
    private double lot;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getDeviceId()
    {
        return deviceId;
    }

    public void setDeviceId(String deviceId)
    {
        this.deviceId = deviceId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Date getSetTime()
    {
        return setTime;
    }

    public void setSetTime(Date setTime)
    {
        this.setTime = setTime;
    }

    public double getRadius()
    {
        return radius;
    }

    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    public double getLat()
    {
        return lat;
    }

    public void setLat(double lat)
    {
        this.lat = lat;
    }

    public double getLot()
    {
        return lot;
    }

    public void setLot(double lot)
    {
        this.lot = lot;
    }

}