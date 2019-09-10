package com.zistone.blowdown_app.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.zistone.blowdown_app.entity.UserInfo;

public class UserSharedPreference
{

    public static void UpdateSuccess(Context context, UserInfo userInfo)
    {
        if(null != userInfo.getM_password())
        {
            UserSharedPreference.SetPassword(context, userInfo.getM_password());
        }
        if(null != userInfo.getM_userImage())
        {
            UserSharedPreference.SetUserImage(context, userInfo.getM_userImage());
        }
    }

    public static void LogoutSuccess(Context context)
    {
        //        UserSharedPreference.SetUserId(context, 0);
        //        UserSharedPreference.SetUserName(context, "");
        //        UserSharedPreference.SetRealName(context, "");
        //        UserSharedPreference.SetPassword(context, "");
        //        UserSharedPreference.SetLevel(context, 1);
        //        UserSharedPreference.SetUserImage(context, "");
        SharedPreferences.Editor editor = Share(context).edit();
        editor.clear();
        editor.commit();
    }

    public static void LoginSuccess(Context context, UserInfo userInfo)
    {
        UserSharedPreference.SetUserId(context, userInfo.getM_id());
        UserSharedPreference.SetUserName(context, userInfo.getM_userName());
        UserSharedPreference.SetRealName(context, userInfo.getM_realName());
        UserSharedPreference.SetPhone(context, userInfo.getM_phoneNumber());
        UserSharedPreference.SetPassword(context, userInfo.getM_password());
        UserSharedPreference.SetState(context, userInfo.getM_state());
        UserSharedPreference.SetLevel(context, userInfo.getM_level());
        UserSharedPreference.SetUserImage(context, userInfo.getM_userImage());
    }

    public static SharedPreferences Share(Context context)
    {
        return context.getSharedPreferences("USER", Context.MODE_PRIVATE);
    }

    public static String GetPhone(Context context)
    {
        return Share(context).getString("userPhone", null);
    }

    public static boolean SetPhone(Context context, String image)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("userPhone", image);
        return editor.commit();
    }

    public static int GetUserId(Context context)
    {
        return Share(context).getInt("userId", 0);
    }

    public static boolean SetUserId(Context context, int id)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("userId", id);
        return editor.commit();
    }

    public static String GetUserImage(Context context)
    {
        return Share(context).getString("userImage", null);
    }

    public static boolean SetUserImage(Context context, String image)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("userImage", image);
        return editor.commit();
    }

    public static String GetUserName(Context context)
    {
        return Share(context).getString("userName", null);
    }

    public static boolean SetUserName(Context context, String name)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("userName", name);
        return editor.commit();
    }

    public static String GetPassword(Context context)
    {
        return Share(context).getString("password", null);
    }

    public static void SetPassword(Context context, String password)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("password", password);
        editor.apply();
    }

    public static int GetLevel(Context context)
    {
        return Share(context).getInt("level", 0);
    }

    public static boolean SetLevel(Context context, int level)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("level", level);
        return editor.commit();
    }

    public static String GetRealName(Context context)
    {
        return Share(context).getString("realName", null);
    }

    public static boolean SetRealName(Context context, String realName)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("realName", realName);
        return editor.commit();
    }

    public static int GetState(Context context)
    {
        return Share(context).getInt("state", 0);
    }

    public static boolean SetState(Context context, int state)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("state", state);
        return editor.commit();
    }

}
