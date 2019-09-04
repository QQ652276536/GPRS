package com.zistone.blowdown_app.control;

import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.util.List;

public class DeviceInfoRecyclerAdapter extends RecyclerView.Adapter<DeviceInfoRecyclerAdapter.DeviceInfoViewHolder>
{
    private Context m_context;
    private List<DeviceInfo> m_list;
    private View m_inflater;
    private OnItemClickListener m_onItemClickListener;

    public interface OnItemClickListener
    {
        void OnClick(int position);

        void OnLongClick(int position);
    }

    public DeviceInfoRecyclerAdapter(Context context, List<DeviceInfo> list)
    {
        m_context = context;
        m_list = list;
    }

    public void SetOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this.m_onItemClickListener = onItemClickListener;
    }

    /**
     * 承载每个子项的布局
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
     * 将每个子项绑定数据
     *
     * @param deviceInfoViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull DeviceInfoViewHolder deviceInfoViewHolder, final int i)
    {
        DeviceInfo deviceInfo = m_list.get(i);
        deviceInfoViewHolder.m_textView.setText(deviceInfo.getM_name() + "\n" + deviceInfo.getM_type());
        if(deviceInfo.getM_state() == 1)
        {
            deviceInfoViewHolder.m_imageView.setImageResource(R.drawable.device3);
            deviceInfoViewHolder.m_textView.setTextColor(m_context.getColor(R.color.colorPrimary));
        }
        else
        {
            deviceInfoViewHolder.m_imageView.setImageResource(R.drawable.device4);
        }
        if(m_onItemClickListener != null)
        {
            deviceInfoViewHolder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    m_onItemClickListener.OnClick(i);
                }
            });
            deviceInfoViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener()
            {
                @Override
                public boolean onLongClick(View v)
                {
                    m_onItemClickListener.OnLongClick(i);
                    return false;
                }
            });
        }
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
        ImageView m_imageView;

        public DeviceInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            m_textView = itemView.findViewById(R.id.text_view);
            m_imageView = itemView.findViewById(R.id.img);
        }
    }

}