package com.zistone.gprs.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.zistone.gprs.R;

import java.text.SimpleDateFormat;

/**
 * 围栏创建对话框
 */
public class DefenseDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Callback _callback;
    private Button _btn1;
    private Button _btn2;

    /**
     * @param activity
     */
    public DefenseDialog(Activity activity, Callback callback)
    {
        super(activity);
        this._callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.defense_dialog);
        _btn1 = findViewById(R.id.btn_warning_defense);
        _btn1.setOnClickListener(this::onClick);
        _btn2 = findViewById(R.id.btn_area_defense);
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
            case R.id.btn_area_defense:
            {
                dismiss();
                if(null != _callback)
                {
                    _callback.onSetCallback();
                }
                break;
            }
            case R.id.btn_warning_defense:
            {
                dismiss();
                if(null != _callback)
                {
                    _callback.onWarnCallback();
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
