package com.zistone.blowdown_app.control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.view.View;

import com.zistone.blowdown_app.R;

public class MyPopWindow extends PopupWindow implements View.OnClickListener
{
    private Context m_context;
    private View m_view;

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.textView1:
                break;
            case R.id.switch1:
                break;
        }
    }

    public MyPopWindow(Context context)
    {
        super(context);
        m_context = context;
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        m_view = LayoutInflater.from(context).inflate(R.layout.map_info_window,null);
        setContentView(m_view);

    }
}
