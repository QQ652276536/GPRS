package com.zistone.blowdown_app.entity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Date;

public class DeviceInfo implements Parcelable
{

    public DeviceInfo()
    {
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>()
    {
        @Override
        public DeviceInfo createFromParcel(Parcel in)
        {
            Bundle bundle = in.readBundle();
            DeviceInfo deviceInfo = new DeviceInfo();
            deviceInfo.setM_id(bundle.getInt("m_id"));
            deviceInfo.setM_deviceName(bundle.getString("m_deviceName"));
            deviceInfo.setM_deviceType(bundle.getString("m_deviceType"));
            deviceInfo.setM_lat(bundle.getFloat("m_lat"));
            deviceInfo.setM_lot(bundle.getFloat("m_lot"));
            deviceInfo.setM_state(bundle.getInt("m_state"));
            return deviceInfo;
        }

        @Override
        public DeviceInfo[] newArray(int size)
        {
            return new DeviceInfo[size];
        }
    };

    @Override
    public String toString()
    {
        return "DeviceInfo{" + "m_id=" + m_id + ", m_deviceName='" + m_deviceName + '\'' + ", m_deviceType='" + m_deviceType + '\'' + ", m_lat=" + m_lat + ", m_lot=" + m_lot + ", m_state=" + m_state + '}';
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

    /**
     * 纬度
     */
    private float m_lat;

    public float getM_lat()
    {
        return m_lat;
    }

    public void setM_lat(float m_lat)
    {
        this.m_lat = m_lat;
    }

    /**
     * 经度
     */
    private float m_lot;

    public float getM_lot()
    {
        return m_lot;
    }

    public void setM_lot(float m_lot)
    {
        this.m_lot = m_lot;
    }

    /**
     * 设备状态:0离线1在线
     */
    private int m_state;

    public int getM_state()
    {
        return m_state;
    }

    public void setM_state(int m_state)
    {
        this.m_state = m_state;
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(m_id);
        dest.writeString(m_deviceName);
        dest.writeString(m_deviceType);
        dest.writeFloat(m_lat);
        dest.writeFloat(m_lot);
        dest.writeInt(m_state);
    }
}