package com.zistone.gprs.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.gprs.util.PropertiesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapFragment_Bind extends Fragment implements View.OnClickListener
{
    private static final String TAG = "MapFragment_Bind";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context _context;
    private View _deviceBindView;
    private ImageButton _btnReturn;
    private OnFragmentInteractionListener _listener;
    private DeviceInfo _deviceInfo;
    private TextView _txtView1;
    private TextView _txtView2;
    private TextView _txtView3;
    private TextView _txtView4;

    /**
     * @param deviceInfo
     * @return
     */
    public static MapFragment_Bind newInstance(DeviceInfo deviceInfo)
    {
        MapFragment_Bind fragment = new MapFragment_Bind();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return_device_bind:
                MapFragment_Choose mapFragment_choose = MapFragment_Choose.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_choose, "mapFragment_choose").commitNow();
                break;
        }
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(_listener != null)
        {
            _listener.onFragmentInteraction(uri);
        }
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch(message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    if(null == result || "".equals(result))
                    {
                        return;
                    }
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "获取数据失败,请与管理员联系", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void SendWithOkHttp()
    {
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //异步请求
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "绑定设备失败:" + e.toString());
                    Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                    handler.sendMessage(message);
                }

                //获得请求响应的字符串:response.body().string()该方法只能被调用一次!另:toString()返回的是对象地址
                //获得请求响应的二进制字节数组:response.body().bytes()
                //获得请求响应的inputStream:response.body().byteStream()
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    String result = response.body().string();
                    if(response.isSuccessful())
                    {
                        Log.i(TAG, "绑定设备成功:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "绑定设备失败:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, result);
                        handler.sendMessage(message);
                    }
                }
            });
            Looper.loop();
        }).start();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            //获取设备信息
            _deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _deviceBindView = inflater.inflate(R.layout.fragment_map_device_bind, container, false);
        _context = _deviceBindView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/Update";
        _btnReturn = _deviceBindView.findViewById(R.id.btn_return_device_bind);
        _btnReturn.setOnClickListener(this::onClick);
        _txtView1 = _deviceBindView.findViewById(R.id.textView1_bind);
        _txtView2 = _deviceBindView.findViewById(R.id.textView2_bind);
        _txtView3 = _deviceBindView.findViewById(R.id.textView3_bind);
        _txtView4 = _deviceBindView.findViewById(R.id.textView4_bind);
        if(null != _deviceInfo)
        {
            _txtView1.setText(_deviceInfo.getName());
            _txtView2.setText("");
            _txtView3.setText(_deviceInfo.getDeviceId());
            _txtView4.setText(_deviceInfo.getSim());
        }
        return _deviceBindView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            _listener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        _listener = null;
    }
}
