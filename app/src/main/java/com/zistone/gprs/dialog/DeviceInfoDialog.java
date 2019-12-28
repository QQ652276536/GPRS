package com.zistone.gprs.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.zistone.gprs.R;
import com.zistone.gprs.entity.DeviceInfo;
import com.zistone.gprs.entity.FenceInfo;
import com.zistone.gprs.fragment.MapFragment_Setting;

import java.text.SimpleDateFormat;

/**
 * 设备信息对话框
 */
public class DeviceInfoDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DeviceInfoDialog.Callback callback;
    private Button m_btn1;
    private Button m_btn2;
    private TextView m_textView1;
    private TextView m_textView3;
    private TextView m_textView4;
    private TextView m_textView5;
    private TextView m_textView6;
    private TextView m_textView7;
    private TextView m_textView8;
    private TextView m_textView9;
    private TextView m_textView10;
    private TextView m_textView11;
    private DeviceInfo m_deviceInfo;
    private String m_deviceAddress;

    /**
     * @param activity
     */
    public DeviceInfoDialog(Activity activity, Callback callback, DeviceInfo deviceInfo, String deviceAddress)
    {
        super(activity);
        this.callback = callback;
        m_deviceInfo = deviceInfo;
        m_deviceAddress = deviceAddress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_info_dialog);
        m_textView1 = findViewById(R.id.text1_deviceInfo_dialog);
        m_textView3 = findViewById(R.id.text3_deviceInfo_dialog);
        m_textView4 = findViewById(R.id.text4_deviceInfo_dialog);
        m_textView5 = findViewById(R.id.text5_deviceInfo_dialog);
        m_textView6 = findViewById(R.id.text6_deviceInfo_dialog);
        m_textView7 = findViewById(R.id.text7_deviceInfo_dialog);
        m_textView8 = findViewById(R.id.text8_deviceInfo_dialog);
        m_textView9 = findViewById(R.id.text9_deviceInfo_dialog);
        m_textView10 = findViewById(R.id.text10_deviceInfo_dialog);
        m_textView11 = findViewById(R.id.text11_deviceInfo_dialog);
        m_btn1 = findViewById(R.id.btn1_deviceInfo_dialog);
        m_btn1.setOnClickListener(this::onClick);
        m_btn2 = findViewById(R.id.btn2_deviceInfo_dialog);
        m_btn2.setOnClickListener(this::onClick);
        if(m_deviceInfo != null)
        {
            m_textView1.setText("车辆跟踪GPRS-1");
            m_textView3.setText(m_deviceInfo.getM_name());
            m_textView4.setText("undefined");
            m_textView5.setText("正常模式");
            m_textView6.setText("10分钟/次");
            m_textView7.setText(SIMPLEDATEFORMAT.format(m_deviceInfo.getM_updateTime()));
            m_textView8.setText(m_deviceInfo.getM_lot() + ", " + m_deviceInfo.getM_lat());
            m_textView9.setText(m_deviceAddress);
            m_textView10.setText(m_deviceInfo.getM_temperature() + "℃");
            m_textView11.setText(m_deviceInfo.getM_electricity() + "%");
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    @Override
    public void onClick(View view)
    {
        switch(view.getId())
        {
            case R.id.btn1_deviceInfo_dialog:
            {
                dismiss();
                break;
            }
            case R.id.btn2_deviceInfo_dialog:
            {
                if(null != callback)
                {
                    callback.onSetCallback();
                }
                dismiss();
                break;
            }
        }
    }

    /**
     * 创建回调接口
     */
    public interface Callback
    {
        /**
         * 取消回调
         */
        void onSetCallback();
    }

}
