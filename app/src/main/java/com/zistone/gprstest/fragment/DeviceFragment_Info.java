package com.zistone.gprstest.fragment;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.gprstest.R;
import com.zistone.gprstest.entity.DeviceInfo;
import com.zistone.gprstest.util.PropertiesUtil;

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

public class DeviceFragment_Info extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceFragment_Info";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context m_context;
    private View m_deviceInfoView;
    private ImageButton m_btnReturn;
    private OnFragmentInteractionListener mListener;
    private DeviceInfo m_deviceInfo;
    private TextView m_toolbartextView;
    private Button m_btnConfirm;
    private EditText m_editText1;
    private EditText m_editText2;
    private EditText m_editText3;
    private EditText m_editText4;
    private EditText m_editText5;
    private Switch m_switch;

    /**
     * @param deviceInfo
     * @return
     */
    public static DeviceFragment_Info newInstance(DeviceInfo deviceInfo)
    {
        DeviceFragment_Info fragment = new DeviceFragment_Info();
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
            case R.id.btn_return_device_info:
                DeviceFragment_List deviceFragment_list = DeviceFragment_List.newInstance(1, "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_list, "deviceFragment_list").commitNow();
                break;
            case R.id.btn_confirm_device_info:
                if(m_btnConfirm.getText().equals("编辑"))
                {
                    m_btnConfirm.setText("保存");
                    m_toolbartextView.setText("设备编辑");
                    SetControlEnabled(true);
                }
                else if(m_btnConfirm.getText().equals("保存"))
                {
                    m_btnConfirm.setText("编辑");
                    m_toolbartextView.setText("设备详情");
                    SetControlEnabled(false);
                    //更新设备信息
                    m_deviceInfo.setM_name(m_editText1.getText().toString());
                    m_deviceInfo.setM_type(m_editText2.getText().toString());
                    m_deviceInfo.setM_deviceId(m_editText3.getText().toString());
                    m_deviceInfo.setM_sim(m_editText4.getText().toString());
                    m_deviceInfo.setM_comment(m_editText5.getText().toString());
                    m_deviceInfo.setM_state(m_switch.isChecked() ? 1 : 0);
                    String jsonData = JSON.toJSONString(m_deviceInfo);
                    new Thread(() ->
                    {
                        Looper.prepare();
                        //实例化并设置连接超时时间、读取超时时间
                        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
                        RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
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

                            @Override
                            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                            {
                                String responseStr = response.body().string();
                                Log.i(TAG, "响应内容:" + responseStr);
                                if(response.isSuccessful())
                                {
                                    Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, responseStr);
                                    handler.sendMessage(message);
                                }
                                else
                                {
                                    Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, responseStr);
                                    handler.sendMessage(message);
                                }
                            }

                        });
                        Looper.loop();
                    }).start();
                }
                break;
            default:
                break;
        }
    }

    private void SetControlEnabled(boolean flag)
    {
        m_editText1.setEnabled(flag);
        m_editText2.setEnabled(flag);
        m_editText3.setEnabled(flag);
        m_editText4.setEnabled(flag);
        m_editText5.setEnabled(flag);
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
        m_context = m_deviceInfoView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/DeviceInfo/Update";
        m_btnReturn = m_deviceInfoView.findViewById(R.id.btn_return_device_info);
        m_btnReturn.setOnClickListener(this::onClick);
        m_toolbartextView = m_deviceInfoView.findViewById(R.id.textView_toolbar_device_info);
        m_btnConfirm = m_deviceInfoView.findViewById(R.id.btn_confirm_device_info);
        m_btnConfirm.setOnClickListener(this::onClick);
        m_editText1 = m_deviceInfoView.findViewById(R.id.editText1_device_info);
        m_editText2 = m_deviceInfoView.findViewById(R.id.editText2_device_info);
        m_editText3 = m_deviceInfoView.findViewById(R.id.editText3_device_info);
        m_editText4 = m_deviceInfoView.findViewById(R.id.editText4_device_info);
        m_editText5 = m_deviceInfoView.findViewById(R.id.editText5_device_info);
        m_switch = m_deviceInfoView.findViewById(R.id.switch_device_info);
        if(null != m_deviceInfo)
        {
            m_editText1.setText(m_deviceInfo.getM_name());
            m_editText2.setText(m_deviceInfo.getM_type());
            m_editText3.setText(m_deviceInfo.getM_deviceId());
            m_editText4.setText(m_deviceInfo.getM_sim());
            m_editText5.setText(m_deviceInfo.getM_comment());
            switch(m_deviceInfo.getM_state())
            {
                case 0:
                    m_switch.setChecked(false);
                    break;
                case 1:
                    m_switch.setChecked(true);
                    break;
                default:
                    break;
            }
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
                    Toast.makeText(m_context, "设备信息更新成功", Toast.LENGTH_SHORT).show();
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
        m_deviceInfoView = inflater.inflate(R.layout.fragment_device_info, container, false);
        InitView();
        return m_deviceInfoView;
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
