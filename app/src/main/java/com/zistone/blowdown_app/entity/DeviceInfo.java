package com.zistone.blowdown_app.entity;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceInfo implements Parcelable
{
    public DeviceInfo()
    {
    }

    @Override
    public String toString()
    {
        return "DeviceInfo{" + "m_id=" + m_id + ", m_deviceId='" + m_deviceId + '\'' + ", m_sim=" + m_sim + ", m_name='" + m_name
                + '\'' + ", m_type='" + m_type + '\'' + ", m_state=" + m_state + ", m_lat=" + m_lat + ", m_lot=" + m_lot + ", m_height="
                + m_height + ", m_createTime=" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_createTime) + ", m_updateTime="
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_updateTime) + ", m_comment='" + m_comment + '\'' + ", m_akCode='"
                + m_akCode + '\'' + '}';
    }

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>()
    {
        @Override
        public DeviceInfo createFromParcel(Parcel in)
        {
            DeviceInfo deviceInfo = new DeviceInfo();
            try
            {
                Bundle bundle = in.readBundle();
                deviceInfo.setM_id(bundle.getInt("m_id"));
                deviceInfo.setM_name(bundle.getString("m_name"));
                deviceInfo.setM_type(bundle.getString("m_type"));
                deviceInfo.setM_lat(bundle.getDouble("m_lat"));
                deviceInfo.setM_lot(bundle.getDouble("m_lot"));
                deviceInfo.setM_state(bundle.getInt("m_state"));
                deviceInfo.setM_deviceId(bundle.getString("m_deviceId"));
                deviceInfo.setM_sim(bundle.getInt("m_sim"));
                deviceInfo.setM_comment(bundle.getString("m_comment"));
                deviceInfo.setM_createTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bundle.getString("m_createTime")));
                deviceInfo.setM_updateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bundle.getString("m_updateTime")));
                deviceInfo.setM_akCode(bundle.getString("m_akCode"));
                deviceInfo.setM_height(bundle.getDouble("m_height"));
            }
            catch(ParseException e)
            {
                e.printStackTrace();
            }
            return deviceInfo;
        }

        @Override
        public DeviceInfo[] newArray(int size)
        {
            return new DeviceInfo[size];
        }
    };

    /**
     * 自增主键(由数据库生成)
     */
    private int m_id;

    /**
     * 设备编号,设备自带
     */
    private String m_deviceId;

    /**
     * SIM卡号
     */
    private int m_sim;

    /**
     * 设备名
     */
    private String m_name;

    /**
     * 设备类型
     */
    public String m_type;

    /**
     * 设备状态:0离线1在线
     */
    private int m_state;

    /**
     * 纬度
     */
    private double m_lat;

    /**
     * 经度
     */
    private double m_lot;
    /**
     * 海拔
     */
    private double m_height;

    /**
     * 创建时间
     */
    private Date m_createTime;

    /**
     * 修改时间
     */
    private Date m_updateTime;

    /**
     * 备注
     */
    private String m_comment;

    /**
     * 鉴权码
     */
    private String m_akCode;

    public int getM_id()
    {
        return m_id;
    }

    public void setM_id(int m_id)
    {
        this.m_id = m_id;
    }

    public String getM_name()
    {
        return m_name;
    }

    public void setM_name(String m_name)
    {
        this.m_name = m_name;
    }

    public String getM_type()
    {
        return m_type;
    }

    public void setM_type(String m_type)
    {
        this.m_type = m_type;
    }

    public int getM_state()
    {
        return m_state;
    }

    public void setM_state(int m_state)
    {
        this.m_state = m_state;
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

    public double getM_height()
    {
        return m_height;
    }

    public void setM_height(double m_height)
    {
        this.m_height = m_height;
    }

    public Date getM_createTime()
    {
        return m_createTime;
    }

    public void setM_createTime(Date m_createTime)
    {
        this.m_createTime = m_createTime;
    }

    public Date getM_updateTime()
    {
        return m_updateTime;
    }

    public void setM_updateTime(Date m_updateTime)
    {
        this.m_updateTime = m_updateTime;
    }

    public String getM_akCode()
    {
        return m_akCode;
    }

    public void setM_akCode(String m_akCode)
    {
        this.m_akCode = m_akCode;
    }

    public String getM_deviceId()
    {
        return m_deviceId;
    }

    public void setM_deviceId(String m_deviceId)
    {
        this.m_deviceId = m_deviceId;
    }

    public int getM_sim()
    {
        return m_sim;
    }

    public void setM_sim(int m_sim)
    {
        this.m_sim = m_sim;
    }

    public String getM_comment()
    {
        return m_comment;
    }

    public void setM_comment(String m_comment)
    {
        this.m_comment = m_comment;
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
        dest.writeString(m_deviceId);
        dest.writeInt(m_sim);
        dest.writeString(m_comment);
        dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_createTime));
        dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(m_updateTime));
        dest.writeString(m_akCode);
        dest.writeDouble(m_height);
    }
}