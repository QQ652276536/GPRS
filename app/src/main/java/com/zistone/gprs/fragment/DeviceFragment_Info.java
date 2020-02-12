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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
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

public class DeviceFragment_Info extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceFragment_Info";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context _context;
    private View _deviceInfoView;
    private ImageButton _btnReturn;
    private OnFragmentInteractionListener _listener;
    private DeviceInfo _deviceInfo;
    private TextView _toolbartextView;
    private Button _btnConfirm;
    private EditText _edt1;
    private EditText _edt2;
    private EditText _edt3;
    private EditText _edt4;
    private EditText _edt5;
    private Switch _switch;

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
        switch (v.getId())
        {
            case R.id.btn_return_device_info:
                DeviceFragment_List deviceFragment_list = DeviceFragment_List.newInstance(1, "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_list, "deviceFragment_list").commitNow();
                break;
            case R.id.btn_confirm_device_info:
                if (_btnConfirm.getText().equals("编辑"))
                {
                    _btnConfirm.setText("保存");
                    _toolbartextView.setText("设备编辑");
                    SetControlEnabled(true);
                }
                else if (_btnConfirm.getText().equals("保存"))
                {
                    _btnConfirm.setText("编辑");
                    _toolbartextView.setText("设备详情");
                    SetControlEnabled(false);
                    //更新设备信息
                    _deviceInfo.setName(_edt1.getText().toString());
                    _deviceInfo.setType(_edt2.getText().toString());
                    _deviceInfo.setDeviceId(_edt3.getText().toString());
                    _deviceInfo.setSim(_edt4.getText().toString());
                    _deviceInfo.setComment(_edt5.getText().toString());
                    _deviceInfo.setState(_switch.isChecked() ? 1 : 0);
                    String jsonData = JSON.toJSONString(_deviceInfo);
                    new Thread(() ->
                               {
                                   Looper.prepare();
                                   //实例化并设置连接超时时间、读取超时时间
                                   OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
                                   RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
                                   Request request = new Request.Builder().post(requestBody).url(URL).build();
                                   Call call = okHttpClient.newCall(request);
                                   //异步请求
                                   call.enqueue(new Callback()
                                   {
                                       @Override
                                       public void onFailure(@NotNull Call call, @NotNull IOException e)
                                       {
                                           Log.e(TAG, "查询设备信息失败:" + e.toString());
                                           Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                                           handler.sendMessage(message);
                                       }

                                       @Override
                                       public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                                       {
                                           String responseStr = response.body().string();
                                           if (response.isSuccessful())
                                           {
                                               Log.i(TAG, "查询设备信息成功:" + responseStr);
                                               Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, responseStr);
                                               handler.sendMessage(message);
                                           }
                                           else
                                           {
                                               Log.e(TAG, "查询设备信息失败:" + responseStr);
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
        _edt1.setEnabled(flag);
        _edt2.setEnabled(flag);
        _edt3.setEnabled(flag);
        _edt4.setEnabled(flag);
        _edt5.setEnabled(flag);
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
        if (_listener != null)
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
            switch (message.what)
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
                    if (null == result || "".equals(result))
                    {
                        return;
                    }
                    Toast.makeText(_context, "设备信息更新成功", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            //获取设备信息
            _deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _deviceInfoView = inflater.inflate(R.layout.fragment_device_info, container, false);
        _context = _deviceInfoView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/Update";
        _btnReturn = _deviceInfoView.findViewById(R.id.btn_return_device_info);
        _btnReturn.setOnClickListener(this::onClick);
        _toolbartextView = _deviceInfoView.findViewById(R.id.textView_toolbar_device_info);
        _btnConfirm = _deviceInfoView.findViewById(R.id.btn_confirm_device_info);
        _btnConfirm.setOnClickListener(this::onClick);
        _edt1 = _deviceInfoView.findViewById(R.id.editText1_device_info);
        _edt2 = _deviceInfoView.findViewById(R.id.editText2_device_info);
        _edt3 = _deviceInfoView.findViewById(R.id.editText3_device_info);
        _edt4 = _deviceInfoView.findViewById(R.id.editText4_device_info);
        _edt5 = _deviceInfoView.findViewById(R.id.editText5_device_info);
        _switch = _deviceInfoView.findViewById(R.id.switch_device_info);
        if (null != _deviceInfo)
        {
            _edt1.setText(_deviceInfo.getName());
            _edt2.setText(_deviceInfo.getType());
            _edt3.setText(_deviceInfo.getDeviceId());
            _edt4.setText(_deviceInfo.getSim());
            _edt5.setText(_deviceInfo.getComment());
            switch (_deviceInfo.getState())
            {
                case 0:
                    _switch.setChecked(false);
                    break;
                case 1:
                    _switch.setChecked(true);
                    break;
                default:
                    break;
            }
        }
        return _deviceInfoView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
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
