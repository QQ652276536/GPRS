package com.zistone.blowdown_app.control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.util.List;

public class DeviceInfoRecyclerAdapter extends RecyclerView.Adapter<DeviceInfoRecyclerAdapter.DeviceInfoViewHolder>
{
    private Context m_context;
    private List<DeviceInfo> m_list;
    private View m_inflater;

    public DeviceInfoRecyclerAdapter(Context context, List<DeviceInfo> list)
    {
        m_context = context;
        m_list = list;
    }

    /**
     * 返回每一项布局
     *
     * @param viewGroup
     * @param i
     * @return
     */
    @NonNull
    @Override
    public DeviceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        m_inflater = LayoutInflater.from(m_context).inflate(R.layout.device_item, viewGroup, false);
        DeviceInfoViewHolder deviceInfoViewHolder = new DeviceInfoViewHolder(m_inflater);
        return deviceInfoViewHolder;
    }

    /**
     * 将数据和控件绑定
     *
     * @param deviceInfoViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull DeviceInfoViewHolder deviceInfoViewHolder, int i)
    {
        deviceInfoViewHolder.m_textView.setText("---------------");
    }

    @Override
    public int getItemCount()
    {
        return m_list.size();
    }

    /**
     * 内部类,用来绑定控件
     */
    class DeviceInfoViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_textView;

        public DeviceInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            m_textView = itemView.findViewById(R.id.text_view);
        }
    }

}