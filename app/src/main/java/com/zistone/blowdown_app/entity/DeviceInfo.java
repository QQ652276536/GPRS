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
            deviceInfo.setM_name(bundle.getString("m_name"));
            deviceInfo.setM_type(bundle.getString("m_type"));
            deviceInfo.setM_lat(bundle.getDouble("m_lat"));
            deviceInfo.setM_lot(bundle.getDouble("m_lot"));
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
        return "DeviceInfo{" + "m_id=" + m_id + ", m_name='" + m_name + '\'' + ", m_type='" + m_type + '\'' + ", m_lat=" + m_lat + ", m_lot=" + m_lot + ", m_state=" + m_state + '}';
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
    private String m_name;

    public String getM_name()
    {
        return m_name;
    }

    public void setM_name(String m_name)
    {
        this.m_name = m_name;
    }

    /**
     * 设备型号
     */
    private String m_type;

    public String getM_type()
    {
        return m_type;
    }

    public void setM_type(String m_type)
    {
        this.m_type = m_type;
    }

    /**
     * 纬度
     */
    private double m_lat;

    public double getM_lat()
    {
        return m_lat;
    }

    public void setM_lat(double m_lat)
    {
        this.m_lat = m_lat;
    }

    /**
     * 经度
     */
    private double m_lot;

    public double getM_lot()
    {
        return m_lot;
    }

    public void setM_lot(double m_lot)
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
        dest.writeString(m_name);
        dest.writeString(m_type);
        dest.writeDouble(m_lat);
        dest.writeDouble(m_lot);
        dest.writeInt(m_state);
    }
}