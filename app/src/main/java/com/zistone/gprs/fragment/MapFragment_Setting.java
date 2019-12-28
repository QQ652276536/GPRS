package com.zistone.gprstest.fragment;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zistone.gprstest.R;
import com.zistone.gprstest.control.MyRadioGroup;
import com.zistone.gprstest.entity.DeviceInfo;
import com.zistone.gprstest.util.ConvertUtil;
import com.zistone.gprstest.util.PropertiesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Calendar;
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
    private EditText m_editText1;
    private EditText m_editText2;
    private EditText m_editText3;
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
                String data = "";
                String timeStr = m_editText1.getText().toString();
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
                if(m_radio1.isChecked() || m_radio2.isChecked())
                {
                    int second;
                    if(m_radio1.isChecked())
                    {
                        String minue = m_editText2.getText().toString();
                        second = Integer.valueOf(minue) * 60;
                    }
                    else
                    {
                        String hour = m_editText3.getText().toString();
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
                    if(m_deviceInfo.getM_type().contains("铱星"))
                    {
                        data =
                                "YX," + m_deviceInfo.getM_deviceId() + ",02" + hexStartTime + hexStrSecond;
                    }
                    else
                    {
                        data =
                                "GPRS," + m_deviceInfo.getM_deviceId() + ",02" + hexStartTime + hexStrSecond;
                    }
                }
                //追踪模式
                else if(m_radio3.isChecked())
                {
                    data = "GPRS," + m_deviceInfo.getM_deviceId() +
                            ",020000000A040000000A0000000B0400000E10";
                }
                if(!data.equals(""))
                {
                    SendWithSocket(data);
                }
                else
                {
                    Toast.makeText(m_context, "请检查参数无误", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.editText_upStart_device_setting:
                //隐藏键盘
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
                m_editText1.clearFocus();
                Calendar calendar = Calendar.getInstance();
                TimePickerDialog.OnTimeSetListener onTimeSetListener = (view, h, m) -> m_editText1.setText(h + ":" + m);
                TimePickerDialog timePickerDialog = new TimePickerDialog(m_context, onTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
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
        m_editText1 = m_deviceSettingView.findViewById(R.id.editText_upStart_device_setting);
        m_editText1.setOnClickListener(this::onClick);
        m_editText2 = m_deviceSettingView.findViewById(R.id.editText_upForMinute_device_setting);
        m_editText3 = m_deviceSettingView.findViewById(R.id.editText_upForHour_device_setting);
        if(null != m_deviceInfo)
        {
            m_textView1.setText(m_deviceInfo.getM_name());
            m_textView2.setText(m_deviceInfo.getM_deviceId());
            m_textView3.setText(m_deviceInfo.getM_sim());
            m_textView4.setText("");
            if(m_deviceInfo.getM_type().contains("铱星"))
            {
                m_radio3.setEnabled(false);
            }
            else
            {
                m_radio3.setEnabled(true);
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
                    Toast.makeText(m_context, "参数设置成功", Toast.LENGTH_SHORT).show();
                }
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
                Toast.makeText(m_context, "参数设置失败,请检查与服务的连接", Toast.LENGTH_SHORT).show();
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
