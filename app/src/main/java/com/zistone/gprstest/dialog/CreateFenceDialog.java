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

import com.baidu.trace.api.fence.FenceShape;
import com.baidu.trace.api.fence.FenceType;
import com.zistone.gprstest.R;

/**
 * 围栏创建对话框
 */
public class CreateFenceDialog extends Dialog implements View.OnClickListener
{
    /**
     * 回调接口
     */
    private Callback callback = null;
    private View fenceRadiusLayout = null;
    private View fenceOffsetLayout = null;
    private Button cancelBtn = null;
    private Button sureBtn = null;
    private EditText fenceDenoiseText = null;
    private EditText fenceRadiusText = null;
    private EditText fenceOffsetText = null;
    private double radius = 1000;
    private int denoise = 0;
    private int offset = 200;

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
        fenceRadiusLayout = findViewById(R.id.layout_fenceCreate_radius);
        fenceOffsetLayout = findViewById(R.id.layout_fenceCreate_offset);
        fenceDenoiseText = findViewById(R.id.edtTxt_fenceCreate_denoise);
        fenceRadiusText = findViewById(R.id.edtTxt_fenceCreate_radius);
        fenceOffsetText = findViewById(R.id.edtTxt_fenceCreate_offset);
        cancelBtn = findViewById(R.id.btn_fenceCreate_cancel);
        sureBtn = findViewById(R.id.btn_fenceCreate_sure);
        cancelBtn.setOnClickListener(this);
        sureBtn.setOnClickListener(this);
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
            case R.id.btn_fenceCreate_cancel:
                dismiss();
                if(null != callback)
                {
                    callback.onCancelCallback();
                }
                break;
            case R.id.btn_fenceCreate_sure:
                String denoiseStr = fenceDenoiseText.getText().toString();
                String radiusStr = fenceRadiusText.getText().toString();
                String offsetStr = fenceOffsetText.getText().toString();
                if(!TextUtils.isEmpty(denoiseStr))
                {
                    try
                    {
                        denoise = Integer.parseInt(denoiseStr);
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if(!TextUtils.isEmpty(radiusStr))
                {
                    try
                    {
                        radius = Double.parseDouble(radiusStr);
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if(!TextUtils.isEmpty(offsetStr))
                {
                    try
                    {
                        offset = Integer.parseInt(offsetStr);
                    }
                    catch(Exception ex)
                    {
                        ex.printStackTrace();
                    }
                }
                if(null != callback)
                {
                    callback.onSureCallback(radius, denoise, offset);
                }
                dismiss();
                break;
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
         * @param radius
         * @param denoise
         * @param offset
         */
        void onSureCallback(double radius, int denoise, int offset);

        /**
         * 取消回调
         */
        void onCancelCallback();
    }

}
