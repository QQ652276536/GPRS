package com.zistone.blowdown_app;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSharedPreference
{
    public static SharedPreferences Share(Context context)
    {
        return context.getSharedPreferences("USER", Context.MODE_PRIVATE);
    }

    public static Object GetUserName(Context context)
    {
        return Share(context).getString("userName", null);
    }

    public static boolean SetUserName(Context context, String name)
    {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putString("userName", name);
        return editor.commit();
    }

    public static Object GetPassword(Context context)
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
}
