package com.example.blowdown_app.http;

import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;

public abstract class LoginCallBack<T>
{
    public Type m_type;

    static Type GetSupperClassTypeParameter(Class<?> subClass)
    {
        //返回直接继承的父类(包含泛型参数)
        Type superClass = subClass.getGenericSuperclass();
        //是否是非Java类的派生类
        if(superClass instanceof Class)
        {
            return null;
        }
        ParameterizedType parameterizedType = (ParameterizedType) superClass;
        //获取泛型
        return $Gson$Types.canonicalize(parameterizedType.getActualTypeArguments()[0]);
    }

    public LoginCallBack()
    {
        m_type = GetSupperClassTypeParameter(this.getClass());
    }

    public abstract void OnSuccess(T t);

    public abstract void OnFail(Call call, IOException e);

    public abstract void OnError(int code);
}
