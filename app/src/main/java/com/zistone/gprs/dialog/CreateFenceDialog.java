package com.zistone.gprs.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.zistone.gprs.R;

/**
 * 围栏创建对话框
 */
public class CreateFenceDialog extends Dialog implements View.OnClickListener
{
    private Callback _callback;
    private Button _btn1 = null;
    private Button _btn2 = null;
    private EditText _edt1 = null;
    private EditText _edt2 = null;
    private EditText _edt3 = null;
    private String _address;

    /**
     * @param activity
     */
    public CreateFenceDialog(Activity activity, Callback callback, String address)
    {
        super(activity);
        this._callback = callback;
        _address = address;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_fence_dialog);
        _edt1 = findViewById(R.id.edt1_createFence);
        _edt2 = findViewById(R.id.edt2_createFence);
        _edt2.setText(_address);
        _edt3 = findViewById(R.id.edt3_createFence);
        _btn1 = findViewById(R.id.btn1_createFence);
        _btn1.setOnClickListener(this::onClick);
        _btn2 = findViewById(R.id.btn2_createFence);
        _btn2.setOnClickListener(this::onClick);
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
            case R.id.btn1_createFence:
            {
                String name = _edt1.getText().toString();
                String address = _edt2.getText().toString();
                double radius = Double.valueOf(_edt3.getText().toString());
                if(name != null && !name.trim().equals("") && radius > 0)
                {
                    if(null != _callback)
                    {
                        _callback.onSureCallback(name, address, radius);
                    }
                }
                else
                {
                    Toast.makeText(getContext(), "请输入围栏名称", Toast.LENGTH_SHORT).show();
                }
                dismiss();
                break;
            }
            case R.id.btn2_createFence:
            {
                dismiss();
                if(null != _callback)
                {
                    _callback.onCancelCallback();
                }
                break;
            }
            default:
                break;
        }
    }

    /**
     * 创建回调接口
     */
    public interface Callback
    {
        /**
         * 确定回调
         *
         * @param name
         * @param address
         * @param radius
         */
        void onSureCallback(String name, String address, double radius);

        /**
         * 取消回调
         */
        void onCancelCallback();
    }

}
