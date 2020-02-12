package com.zistone.gprs.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;

import java.text.SimpleDateFormat;

/**
 * 设备信息对话框
 */
public class DeviceInfoDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DeviceInfoDialog.Callback _callback;
    private Button _btn1;
    private Button _btn2;
    private TextView _txtView1;
    private TextView _txtView3;
    private TextView _txtView4;
    private TextView _txtView5;
    private TextView _txtView6;
    private TextView _txtView7;
    private TextView _txtView8;
    private TextView _txtView9;
    private TextView _txtView10;
    private TextView _txtView11;
    private DeviceInfo _deviceInfo;
    private String _deviceAddress;

    /**
     * @param activity
     */
    public DeviceInfoDialog(Activity activity, Callback callback, DeviceInfo deviceInfo, String deviceAddress)
    {
        super(activity);
        this._callback = callback;
        _deviceInfo = deviceInfo;
        _deviceAddress = deviceAddress;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_info_dialog);
        _txtView1 = findViewById(R.id.text1_deviceInfo_dialog);
        _txtView3 = findViewById(R.id.text3_deviceInfo_dialog);
        _txtView4 = findViewById(R.id.text4_deviceInfo_dialog);
        _txtView5 = findViewById(R.id.text5_deviceInfo_dialog);
        _txtView6 = findViewById(R.id.text6_deviceInfo_dialog);
        _txtView7 = findViewById(R.id.text7_deviceInfo_dialog);
        _txtView8 = findViewById(R.id.text8_deviceInfo_dialog);
        _txtView9 = findViewById(R.id.text9_deviceInfo_dialog);
        _txtView10 = findViewById(R.id.text10_deviceInfo_dialog);
        _txtView11 = findViewById(R.id.text11_deviceInfo_dialog);
        _btn1 = findViewById(R.id.btn1_deviceInfo_dialog);
        _btn1.setOnClickListener(this::onClick);
        _btn2 = findViewById(R.id.btn2_deviceInfo_dialog);
        _btn2.setOnClickListener(this::onClick);
        if(_deviceInfo != null)
        {
            _txtView1.setText("车辆跟踪GPRS-1");
            _txtView3.setText(_deviceInfo.getName());
            _txtView4.setText("undefined");
            _txtView5.setText("正常模式");
            _txtView6.setText("10分钟/次");
            _txtView7.setText(SIMPLEDATEFORMAT.format(_deviceInfo.getUpdateTime()));
            _txtView8.setText(_deviceInfo.getLot() + ", " + _deviceInfo.getLat());
            _txtView9.setText(_deviceAddress);
            _txtView10.setText(_deviceInfo.getTemperature() + "℃");
            _txtView11.setText(_deviceInfo.getElectricity() + "%");
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
                if(null != _callback)
                {
                    _callback.onSetCallback();
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
