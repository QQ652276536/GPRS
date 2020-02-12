package com.zistone.gprs.pojo;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Date;

public class UserInfo
{
    @Override
    public String toString()
    {
        return "UserInfo{" +
                "id=" + id +
                ", userImage='" + userImage + '\'' +
                ", userName='" + userName + '\'' +
                ", realName='" + realName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", level=" + level +
                ", state=" + state +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", password='" + password + '\'' +
                '}';
    }

    /**
     * 用户编号(由数据库生成)
     */
    private int id;

    /**
     * 用户头像
     * 存储的是通过Base64转换成的字符串
     */
    private String userImage;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 用户实名
     */
    private String realName;

    /**
     * 注册手机号
     */
    private String phoneNumber;

    /**
     * 用户权限
     */
    private int level;

    /**
     * 用户状态
     */
    private int state;

    /**
     * 创建时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 修改时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 密码
     */
    private String password;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getUserImage()
    {
        return userImage;
    }

    public void setUserImage(String userImage)
    {
        this.userImage = userImage;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public String getRealName()
    {
        return realName;
    }

    public void setRealName(String realName)
    {
        this.realName = realName;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public int getLevel()
    {
        return level;
    }

    public void setLevel(int level)
    {
        this.level = level;
    }

    public int getState()
    {
        return state;
    }

    public void setState(int state)
    {
        this.state = state;
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

}
