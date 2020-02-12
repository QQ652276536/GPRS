package com.zistone.gprs.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.gprs.R;
import com.zistone.gprs.pojo.FenceInfo;

import java.text.SimpleDateFormat;

/**
 * 围栏创建对话框
 */
public class FenceInfoDialog extends Dialog implements View.OnClickListener
{
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Callback _callback;
    private Button _btn1;
    private Button _btn2;
    private TextView _txtView1;
    private TextView _txtView2;
    private TextView _txtView3;
    private TextView _txtView4;
    private FenceInfo _fenceInfo;

    /**
     * @param activity
     */
    public FenceInfoDialog(Activity activity, Callback callback, FenceInfo fenceInfo)
    {
        super(activity);
        this._callback = callback;
        _fenceInfo = fenceInfo;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fence_info_dialog);
        _txtView1 = findViewById(R.id.text1_infoFence);
        _txtView2 = findViewById(R.id.text2_infoFence);
        _txtView3 = findViewById(R.id.text3_infoFence);
        _txtView4 = findViewById(R.id.text4_infoFence);
        _btn1 = findViewById(R.id.btn1_infoFence);
        _btn1.setOnClickListener(this::onClick);
        _btn2 = findViewById(R.id.btn2_infoFence);
        _btn2.setOnClickListener(this::onClick);
        if(_fenceInfo != null)
        {
            _txtView1.setText(_fenceInfo.getName());
            _txtView2.setText(_fenceInfo.getAddress());
            _txtView3.setText(SIMPLEDATEFORMAT.format(_fenceInfo.getSetTime()));
            _txtView4.setText(_fenceInfo.getRadius() + "");
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
                if(null != _callback)
                {
                    _callback.onDelCallback();
                }
                break;
            }
            case R.id.btn2_infoFence:
            {
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
        void onDelCallback();
    }

}
