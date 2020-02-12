package com.zistone.gprs.fragment;

import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.gprs.util.ConvertUtil;
import com.zistone.gprs.util.PropertiesUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;

public class MapFragment_Setting extends Fragment implements View.OnClickListener
{
    private static final String TAG = "MapFragment_Setting";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context _context;
    private View _deviceSettingView;
    private ImageButton _btnReturn;
    private OnFragmentInteractionListener _listener;
    private DeviceInfo _deviceInfo;
    private Button _btnSave;
    private TextView _txtView1;
    private TextView _txtView2;
    private TextView _txtView3;
    private TextView _txtView4;
    private EditText _edt1;
    private EditText _edt2;
    private EditText _edt3;
    private RadioButton _rbtn1;
    private RadioButton _rbtn2;
    private RadioButton _rbtn3;

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
                MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").commitNow();
                break;
            case R.id.btn_confirm_device_device_setting:
                String data = "";
                String timeStr = _edt1.getText().toString();
                String time1 = timeStr.split(":")[0];
                String time2 = timeStr.split(":")[1];
                if(time1.length() < 2)
                {
                    time1 = "0" + time1;
                }
                if(time2.length() < 2)
                {
                    time2 = "0" + time2;
                }
                //每天上报起始时间
                String hexStartTime = "000000090400" + time1 + time2 + "00";
                //监控模式
                if(_rbtn1.isChecked() || _rbtn2.isChecked())
                {
                    int second;
                    if(_rbtn1.isChecked())
                    {
                        String minue = _edt2.getText().toString();
                        second = Integer.valueOf(minue) * 60;
                    }
                    else
                    {
                        String hour = _edt3.getText().toString();
                        second = Integer.valueOf(hour) * 60 * 60;
                    }
                    String hexStrSecond = ConvertUtil.IntToHexStr(second);
                    int i = 8 - hexStrSecond.length();
                    StringBuffer stringBuffer = new StringBuffer(hexStrSecond);
                    for(; i > 0; i--)
                    {
                        stringBuffer.insert(0, "0");
                    }
                    //上报间隔,秒
                    hexStrSecond = "0000002904" + stringBuffer.toString();
                    if(_deviceInfo.getType().contains("铱星"))
                    {
                        data = "YX," + _deviceInfo.getDeviceId() + ",02" + hexStartTime + hexStrSecond;
                    }
                    else
                    {
                        data = "GPRS," + _deviceInfo.getDeviceId() + ",02" + hexStartTime + hexStrSecond;
                    }
                }
                //追踪模式
                else if(_rbtn3.isChecked())
                {
                    data = "GPRS," + _deviceInfo.getDeviceId() + ",020000000A040000000A0000000B0400000E10";
                }
                if(!data.equals(""))
                {
                    SendWithSocket(data);
                }
                else
                {
                    Toast.makeText(_context, "请检查参数无误", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.editText_upStart_device_setting:
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                _edt1.clearFocus();
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> _edt1.setText(h + ":" + m);
                TimePickerDialog timePickerDialog = new TimePickerDialog(_context, onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
                timePickerDialog.show();
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

    private void SendWithSocket(String data)
    {
        new Thread(() ->
        {
            Looper.prepare();
            Socket socket;
            try
            {
                socket = new Socket("129.204.165.206", 10799);
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                dataOutputStream.writeUTF(data);
                dataOutputStream.flush();
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                String result = dataInputStream.readUTF();
                if(result.contains("OK"))
                {
                    Toast.makeText(_context, "参数设置成功", Toast.LENGTH_SHORT).show();
                }
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                Toast.makeText(_context, "参数设置失败,请检查与服务的连接", Toast.LENGTH_SHORT).show();
            }
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
        _deviceSettingView = inflater.inflate(R.layout.fragment_map_device_setting, container, false);
        _context = _deviceSettingView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/Update";
        _btnReturn = _deviceSettingView.findViewById(R.id.btn_return_device_device_setting);
        _btnReturn.setOnClickListener(this::onClick);
        _btnSave = _deviceSettingView.findViewById(R.id.btn_confirm_device_device_setting);
        _btnSave.setOnClickListener(this::onClick);
        _txtView1 = _deviceSettingView.findViewById(R.id.textView1_device_setting);
        _txtView2 = _deviceSettingView.findViewById(R.id.textView2_device_setting);
        _txtView3 = _deviceSettingView.findViewById(R.id.textView3_device_setting);
        _txtView4 = _deviceSettingView.findViewById(R.id.textView4_device_setting);
        _rbtn1 = _deviceSettingView.findViewById(R.id.radio1_setting);
        _rbtn2 = _deviceSettingView.findViewById(R.id.radio2_setting);
        _rbtn3 = _deviceSettingView.findViewById(R.id.radio3_setting);
        _edt1 = _deviceSettingView.findViewById(R.id.editText_upStart_device_setting);
        _edt1.setOnClickListener(this::onClick);
        _edt2 = _deviceSettingView.findViewById(R.id.editText_upForMinute_device_setting);
        _edt3 = _deviceSettingView.findViewById(R.id.editText_upForHour_device_setting);
        if(null != _deviceInfo)
        {
            _txtView1.setText(_deviceInfo.getName());
            _txtView2.setText(_deviceInfo.getDeviceId());
            _txtView3.setText(_deviceInfo.getSim());
            _txtView4.setText("");
            if(_deviceInfo.getType().contains("铱星"))
            {
                _rbtn3.setEnabled(false);
            }
            else
            {
                _rbtn3.setEnabled(true);
            }
        }
        return _deviceSettingView;
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
