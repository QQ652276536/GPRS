package com.zistone.gprstest.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.zistone.gprstest.R;
import com.zistone.gprstest.entity.FenceInfo;

import java.text.SimpleDateFormat;

/**
 * 围栏创建对话框
 */
public class DefenseDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Callback callback;
    private Button m_btn1;
    private Button m_btn2;

    /**
     * @param activity
     */
    public DefenseDialog(Activity activity, Callback callback)
    {
        super(activity);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.defense_dialog);
        m_btn1 = findViewById(R.id.btn_warning_defense);
        m_btn1.setOnClickListener(this::onClick);
        m_btn2 = findViewById(R.id.btn_area_defense);
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
            case R.id.btn1_infoFence:
            {
                dismiss();
                if(null != callback)
                {
                    callback.onSetCallback();
                }
                break;
            }
            case R.id.btn2_infoFence:
            {
                dismiss();
                if(null != callback)
                {
                    callback.onWarnCallback();
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
        void onSetCallback();

        void onWarnCallback();
    }

}
