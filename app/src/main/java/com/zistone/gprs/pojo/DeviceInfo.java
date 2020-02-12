package com.zistone.gprs.pojo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DeviceInfo implements Parcelable
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final Creator<DeviceInfo> CREATOR = new Creator<DeviceInfo>()
    {
        @Override
        public DeviceInfo createFromParcel(Parcel in)
        {
            DeviceInfo deviceInfo = new DeviceInfo();
            Bundle bundle = in.readBundle();
            deviceInfo.setId(bundle.getInt("id"));
            deviceInfo.setName(bundle.getString("name"));
            deviceInfo.setType(bundle.getString("type"));
            deviceInfo.setLat(bundle.getDouble("lat"));
            deviceInfo.setLot(bundle.getDouble("lot"));
            deviceInfo.setState(bundle.getInt("state"));
            deviceInfo.setDeviceId(bundle.getString("deviceId"));
            deviceInfo.setSim(bundle.getString("sim"));
            deviceInfo.setComment(bundle.getString("comment"));
            try
            {
                deviceInfo.setCreateTime(SIMPLEDATEFORMAT.parse(bundle.getString("createTime")));
                deviceInfo.setUpdateTime(SIMPLEDATEFORMAT.parse(bundle.getString("updateTime")));
            }
            catch(ParseException e)
            {
                e.printStackTrace();
            }
            deviceInfo.setAkCode(bundle.getString("akCode"));
            deviceInfo.setHeight(bundle.getDouble("height"));
            deviceInfo.setTemperature(bundle.getInt("temperature"));
            deviceInfo.setElectricity(bundle.getInt("electricity"));
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
        return "DeviceInfo{" +
                "id=" + id +
                ", deviceId='" + deviceId + '\'' +
                ", sim='" + sim + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", state=" + state +
                ", lat=" + lat +
                ", lot=" + lot +
                ", height=" + height +
                ", createTime=" + (createTime != null ? SIMPLEDATEFORMAT.format(createTime) : null) +
                ", updateTime=" + (updateTime != null ? SIMPLEDATEFORMAT.format(updateTime) : null) +
                ", comment='" + comment + '\'' +
                ", akCode='" + akCode + '\'' +
                ", temperature=" + temperature +
                ", electricity=" + electricity +
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
     * SIM卡号
     */
    private String sim;

    /**
     * 设备名
     */
    private String name;

    /**
     * 设备类型
     */
    public String type;

    /**
     * 设备状态:0离线1在线
     */
    private int state;

    /**
     * 纬度
     */
    private double lat;

    /**
     * 经度
     */
    private double lot;
    /**
     * 海拔
     */
    private double height;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 备注
     */
    private String comment;

    /**
     * 鉴权码,注册成功后才有,由Web服务随机生成
     */
    private String akCode;

    /**
     * 温度
     */
    private int temperature;

    /**
     * 剩余电量
     */
    private int electricity;

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeDouble(lat);
        dest.writeDouble(lot);
        dest.writeInt(state);
        dest.writeString(deviceId);
        dest.writeString(sim);
        dest.writeString(comment);
        if(null != createTime)
        {
            dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime));
        }
        else
        {
            dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        if(null != updateTime)
        {
            dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(updateTime));
        }
        else
        {
            dest.writeString(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }
        dest.writeString(akCode);
        dest.writeDouble(height);
        dest.writeInt(temperature);
        dest.writeInt(electricity);
    }

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

    public String getSim()
    {
        return sim;
    }

    public void setSim(String sim)
    {
        this.sim = sim;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
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

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getAkCode()
    {
        return akCode;
    }

    public void setAkCode(String akCode)
    {
        this.akCode = akCode;
    }

    public int getTemperature()
    {
        return temperature;
    }

    public void setTemperature(int temperature)
    {
        this.temperature = temperature;
    }

    public int getElectricity()
    {
        return electricity;
    }

    public void setElectricity(int electricity)
    {
        this.electricity = electricity;
    }

}