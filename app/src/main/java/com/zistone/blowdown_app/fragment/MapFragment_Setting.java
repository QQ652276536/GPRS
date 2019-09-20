package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.chip.ChipGroup;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.MyRadioGroup;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.util.PropertiesUtil;

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

public class MapFragment_Setting extends Fragment implements View.OnClickListener
{
    private static final String TAG = "MapFragment_Setting";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context m_context;
    private View m_deviceSettingView;
    private ImageButton m_btnReturn;
    private OnFragmentInteractionListener mListener;
    private DeviceInfo m_deviceInfo;
    private Button m_btnSave;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;
    private TextView m_textView4;
    private RadioButton m_radio1;
    private RadioButton m_radio2;
    private RadioButton m_radio3;

    /**
     * @param deviceInfo
     * @return
     */
    public static MapFragment_Setting newInstance(DeviceInfo deviceInfo)
    {
        MapFragment_Setting fragment = new MapFragment_Setting();
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
            case R.id.btn_return_device_device_setting:
                MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(m_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").commitNow();
                break;
            case R.id.btn_confirm_device_device_setting:
                if(m_radio1.isChecked())
                {
                }
                else if(m_radio2.isChecked())
                {
                }
                else if(m_radio3.isChecked())
                {
                }
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
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = m_deviceSettingView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/DeviceInfo/Update";
        m_btnReturn = m_deviceSettingView.findViewById(R.id.btn_return_device_device_setting);
        m_btnReturn.setOnClickListener(this::onClick);
        m_btnSave = m_deviceSettingView.findViewById(R.id.btn_confirm_device_device_setting);
        m_btnSave.setOnClickListener(this::onClick);
        m_textView1 = m_deviceSettingView.findViewById(R.id.textView1_device_setting);
        m_textView2 = m_deviceSettingView.findViewById(R.id.textView2_device_setting);
        m_textView3 = m_deviceSettingView.findViewById(R.id.textView3_device_setting);
        m_textView4 = m_deviceSettingView.findViewById(R.id.textView4_device_setting);
        m_radio1 = m_deviceSettingView.findViewById(R.id.radio1_setting);
        m_radio2 = m_deviceSettingView.findViewById(R.id.radio2_setting);
        m_radio3 = m_deviceSettingView.findViewById(R.id.radio3_setting);
        if(null != m_deviceInfo)
        {
            m_textView1.setText(m_deviceInfo.getM_name());
            m_textView2.setText(m_deviceInfo.getM_deviceId());
            m_textView3.setText(String.valueOf(m_deviceInfo.getM_sim()));
            m_textView4.setText("");
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
                    Toast.makeText(m_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(m_context, "获取数据失败,请与管理员联系", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 用OkHttp发送网络请求,并在里面开启线程
     */
    private void SendWithOkHttp()
    {
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
            //创建Post请求的方式
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //Android中不允许任何网络的交互在主线程中进行
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "请求失败:" + e.toString());
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
                    Log.i(TAG, "响应内容:" + result);
                    if(response.isSuccessful())
                    {
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
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
            m_deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceSettingView = inflater.inflate(R.layout.fragment_map_device_setting, container, false);
        InitView();
        return m_deviceSettingView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
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
        mListener = null;
    }
}
