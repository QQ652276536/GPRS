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
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceFragment_Add extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceFragment_Add";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context _context;
    private View _addDeviceView;
    private ImageButton _btnReturn;
    private OnFragmentInteractionListener _listener;
    private EditText _edt_deviceName;
    private EditText _edt_deviceType;
    private EditText _edt_deviceId;
    private EditText _edt_simNumber;
    private EditText _edt_comment;
    private Switch _switch_state;
    private Button _btnSave;
    private ProgressBar _progressBar;

    public static DeviceFragment_Add newInstance(String param1, String param2)
    {
        DeviceFragment_Add fragment = new DeviceFragment_Add();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void IsAdding()
    {
        _progressBar.setVisibility(View.VISIBLE);
        _edt_deviceName.setEnabled(false);
        _edt_deviceType.setEnabled(false);
        _edt_deviceId.setEnabled(false);
        _edt_simNumber.setEnabled(false);
        _edt_comment.setEnabled(false);
        _switch_state.setEnabled(false);
    }

    private void IsAddEnd()
    {
        _progressBar.setVisibility(View.INVISIBLE);
        _edt_deviceName.setEnabled(true);
        _edt_deviceType.setEnabled(true);
        _edt_deviceId.setEnabled(true);
        _edt_simNumber.setEnabled(true);
        _edt_comment.setEnabled(true);
        _switch_state.setEnabled(true);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            IsAddEnd();
            switch (message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "添加设备超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    DeviceInfo deviceInfo = JSON.parseObject(result, DeviceInfo.class);
                    if (deviceInfo != null)
                    {
                        Log.i(TAG, "设备添加成功,设备编号为:" + deviceInfo.getDeviceId());
                        DeviceFragment_Manage deviceFragment_manage = DeviceFragment_Manage.newInstance("", "");
                        getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_manage, "deviceFragment_manage").commitNow();
                        Toast.makeText(_context, "设备添加成功", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Log.i(TAG, "设备添加失败");
                        Toast.makeText(_context, "设备添加失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "添加设备失败", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_return_device_add:
                DeviceFragment_Manage deviceFragment_manage = DeviceFragment_Manage.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_manage, "deviceFragment_manage").commitNow();
                break;
            case R.id.btn_save_add:
                String name = _edt_deviceName.getText().toString();
                String type = _edt_deviceType.getText().toString();
                String deviceId = _edt_deviceId.getText().toString();
                String sim = _edt_simNumber.getText().toString();
                String comment = _edt_comment.getText().toString();
                boolean state = _switch_state.isChecked();
                if (null == name || "".equals(name) || null == type || "".equals(type) || null == deviceId || "".equals(deviceId))
                {
                    Toast.makeText(_context, "请填写正确的设备信息", Toast.LENGTH_SHORT).show();
                    return;
                }
                IsAdding();
                DeviceInfo deviceInfo = new DeviceInfo();
                deviceInfo.setName(name);
                deviceInfo.setType(type);
                deviceInfo.setDeviceId(deviceId);
                deviceInfo.setSim(sim);
                deviceInfo.setComment(comment);
                deviceInfo.setState(state ? 1 : 0);
                deviceInfo.setCreateTime(new Date());
                deviceInfo.setUpdateTime(new Date());
                String jsonData = JSON.toJSONString(deviceInfo);
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
                            Log.e(TAG, "添加设备失败:" + e.toString());
                            Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                            handler.sendMessage(message);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                        {
                            String responseStr = response.body().string();
                            if (response.isSuccessful())
                            {
                                Log.i(TAG, "添加设备成功:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, responseStr);
                                handler.sendMessage(message);
                            }
                            else
                            {
                                Log.e(TAG, "添加设备失败:" + responseStr);
                                Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, responseStr);
                                handler.sendMessage(message);
                            }
                        }
                    });
                    Looper.loop();
                }).start();
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
        if (_listener != null)
        {
            _listener.onFragmentInteraction(uri);
        }
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
        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _addDeviceView = inflater.inflate(R.layout.fragment_device_add, container, false);

        _context = getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/InsertByDeviceId";
        _btnReturn = _addDeviceView.findViewById(R.id.btn_return_device_add);
        _btnReturn.setOnClickListener(this);
        _edt_deviceName = _addDeviceView.findViewById(R.id.editText_deviceName_add);
        _edt_deviceType = _addDeviceView.findViewById(R.id.editText_deviceType_add);
        _edt_deviceId = _addDeviceView.findViewById(R.id.editText_deviceID_add);
        _edt_simNumber = _addDeviceView.findViewById(R.id.editText_sim_number_add);
        _edt_comment = _addDeviceView.findViewById(R.id.editText_comment_add);
        _switch_state = _addDeviceView.findViewById(R.id.switch_state_add);
        _progressBar = _addDeviceView.findViewById(R.id.progressBar_add);
        _btnSave = _addDeviceView.findViewById(R.id.btn_save_add);
        _btnSave.setOnClickListener(this);
        return _addDeviceView;
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
