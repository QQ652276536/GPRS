package com.zistone.gprstest.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.model.LatLng;
import com.zistone.gprstest.R;
import com.zistone.gprstest.entity.FenceInfo;

import java.text.SimpleDateFormat;

/**
 * 围栏创建对话框
 */
public class InfoFenceDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Callback callback;
    private ImageButton m_btn1;
    private Button m_btn2;
    private TextView m_textView1;
    private TextView m_textView2;
    private TextView m_textView3;
    private TextView m_textView4;
    private FenceInfo m_fenceInfo;

    /**
     * @param activity
     */
    public InfoFenceDialog(Activity activity, Callback callback, FenceInfo fenceInfo)
    {
        super(activity, android.R.style.Theme_Holo_Light_Dialog);
        this.callback = callback;
        m_fenceInfo = fenceInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.info_fence_dialog);
        m_textView1 = findViewById(R.id.text1_infoFence);
        m_textView2 = findViewById(R.id.text2_infoFence);
        m_textView3 = findViewById(R.id.text3_infoFence);
        m_textView4 = findViewById(R.id.text4_infoFence);
        m_btn1 = findViewById(R.id.btn1_infoFence);
        m_btn1.setOnClickListener(this::onClick);
        m_btn2 = findViewById(R.id.btn2_infoFence);
        m_btn2.setOnClickListener(this::onClick);
        if(m_fenceInfo != null)
        {
            m_textView1.setText(m_fenceInfo.getM_name());
            m_textView2.setText(m_fenceInfo.getM_address());
            m_textView3.setText(SIMPLEDATEFORMAT.format(m_fenceInfo.getM_setTime()));
            m_textView4.setText(m_fenceInfo.getM_radius() + "");
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
            case R.id.btn1_infoFence:
            {
                dismiss();
                break;
            }
            case R.id.btn2_infoFence:
            {
                dismiss();
                if(null != callback)
                {
                    callback.onDelCallback();
                }
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
        void onDelCallback();
    }

}
