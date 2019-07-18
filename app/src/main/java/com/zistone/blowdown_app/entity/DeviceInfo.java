package com.zistone.blowdown_app.entity;

import java.util.Date;

public class DeviceInfo
{

    public DeviceInfo()
    {
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" + "m_id=" + m_id + ", m_deviceName='" + m_deviceName + '\'' + ", m_deviceType='" + m_deviceType + '\'' + '}';
    }

    /**
     * 设备编号(由数据库生成)
     */
    private int m_id;

    public void setM_id(int m_id)
    {
        this.m_id = m_id;
    }

    public int getM_id()
    {
        return m_id;
    }

    /**
     * 设备名
     */
    private String m_deviceName;

    public String getM_deviceName()
    {
        return m_deviceName;
    }

    public void setM_deviceName(String m_deviceName)
    {
        this.m_deviceName = m_deviceName;
    }

    /**
     * 设备型号
     */
    private String m_deviceType;

    public String getM_deviceType()
    {
        return m_deviceType;
    }

    public void setM_deviceType(String m_deviceType)
    {
        this.m_deviceType = m_deviceType;
    }
}