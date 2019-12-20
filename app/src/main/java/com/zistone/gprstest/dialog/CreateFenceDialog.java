package com.zistone.gprstest.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.model.LatLng;
import com.zistone.gprstest.R;

/**
 * 围栏创建对话框
 */
public class CreateFenceDialog extends Dialog implements View.OnClickListener
{
    private Callback callback;
    private Button m_btn1 = null;
    private Button m_btn2 = null;
    private EditText m_editText1 = null;
    private EditText m_editText2 = null;
    private EditText m_editText3 = null;

    /**
     * @param activity
     */
    public CreateFenceDialog(Activity activity, Callback callback)
    {
        super(activity, android.R.style.Theme_Holo_Light_Dialog);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_fence_dialog);
        m_editText1 = findViewById(R.id.edt1_createFence);
        m_editText2 = findViewById(R.id.edt2_createFence);
        m_editText3 = findViewById(R.id.edt3_createFence);
        m_btn1 = findViewById(R.id.btn1_createFence);
        m_btn1.setOnClickListener(this::onClick);
        m_btn2 = findViewById(R.id.btn2_createFence);
        m_btn2.setOnClickListener(this::onClick);
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
                String name = m_editText1.getText().toString();
                String address = m_editText2.getText().toString();
                double radius = Double.valueOf(m_editText3.getText().toString());
                if(name != null && !name.trim().equals("") && radius > 0)
                {
                    if(null != callback)
                    {
                        callback.onSureCallback(name, address, radius);
                    }
                }
                else
                {
                }
                dismiss();
                break;
            }
            case R.id.btn2_createFence:
            {
                dismiss();
                if(null != callback)
                {
                    callback.onCancelCallback();
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
