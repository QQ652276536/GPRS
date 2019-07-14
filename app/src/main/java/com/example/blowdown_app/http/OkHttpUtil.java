package com.example.blowdown_app.http;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.blowdown_app.entity.UserInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpUtil
{
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    private static String m_resultStr = "";

    public OkHttpUtil()
    {
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch (message.what)
            {
                case MESSAGE_GETRESPONSE_SUCCESS:
                    m_resultStr = (String) message.obj;
                    break;
                case MESSAGE_GETRESPONSE_FAIL:
                    m_resultStr = (String) message.obj;
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 异步发送Post请求,使用Handler处理消息
     * @param url
     * @param map
     */
    public void SendByPost(String url, Map<String, String> map)
    {
        //实例化并设置连接超时时间、读取超时时间
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();
        Gson gson = new Gson();
        String data = gson.toJson(map);
        RequestBody requestBody = FormBody.create(data, MediaType.parse("application/json; charset=utf-8"));
        //创建Post请求的方式
        Request request = new Request.Builder().post(requestBody).url(url).build();
        Call call = okHttpClient.newCall(request);
        //Android中不允许任何网络的交互在主线程中进行
        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e)
            {
                Log.e("LoginLog", "请求失败:" + e.toString());
            }

            //获得请求响应的字符串:response.body().string()该方法只能被调用一次!另:toString()返回的是对象地址
            //获得请求响应的二进制字节数组:response.body().bytes()
            //获得请求响应的inputStream:response.body().byteStream()
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    String tempStr = response.body().string();
                    Log.i("LoginLog", "收到Post请求的响应内容:" + tempStr);
                    Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_SUCCESS,tempStr);
                    handler.sendMessage(message);
                }
                else
                {
                    String tempStr = response.body().string();
                    Log.i("LoginLog", "收到Post请求的响应内容:" + tempStr);
                    Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_FAIL,tempStr);
                    handler.sendMessage(message);
                }
            }
        });
    }

    /**
     * 异步发送Post请求,使用回调处理消息
     * @param url
     * @param map
     * @param listener
     */
    public void SendByPost(String url, Map<String, String> map,LoginCallBackListener listener)
    {
        //实例化并设置连接超时时间、读取超时时间
        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS).build();
        Gson gson = new Gson();
        String data = gson.toJson(map);
        RequestBody requestBody = FormBody.create(data, MediaType.parse("application/json; charset=utf-8"));
        //创建Post请求的方式
        Request request = new Request.Builder().post(requestBody).url(url).build();
        Call call = okHttpClient.newCall(request);
        //Android中不允许任何网络的交互在主线程中进行
        call.enqueue(listener);
    }

    public String GetSendByPost()
    {
        return m_resultStr;
    }
}
