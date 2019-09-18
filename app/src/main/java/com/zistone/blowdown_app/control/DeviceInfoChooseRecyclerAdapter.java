package com.zistone.blowdown_app.control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceInfoChooseRecyclerAdapter extends RecyclerView.Adapter<DeviceInfoChooseRecyclerAdapter.DeviceInfoChooseViewHolder>
{
    private static final String TAG = "DeviceInfoRecyclerAdapt";
    private Context m_context;
    private List<DeviceInfo> m_list;
    private View m_inflater;
    private OnItemClickListener m_onItemClickListener;
    private OnClickListener m_onClickListener;

    public interface OnItemClickListener
    {
        void OnClick(int position);

        void OnLongClick(int position);
    }

    public interface OnClickListener
    {
        void OnClick(int position);
    }

    public DeviceInfoChooseRecyclerAdapter(Context context, List<DeviceInfo> list)
    {
        m_context = context;
        m_list = list;
    }

    public void SetOnClickListener(OnClickListener onClickListener)
    {
        this.m_onClickListener = onClickListener;
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
    public DeviceInfoChooseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        m_inflater = LayoutInflater.from(m_context).inflate(R.layout.device_item_choose, viewGroup, false);
        DeviceInfoChooseViewHolder deviceInfoChooseViewHolder = new DeviceInfoChooseViewHolder(m_inflater);
        return deviceInfoChooseViewHolder;
    }

    /**
     * 将每个子项绑定数据
     *
     * @param deviceInfoViewHolder
     * @param i
     */
    @Override
    public void onBindViewHolder(@NonNull DeviceInfoChooseViewHolder deviceInfoViewHolder, final int i)
    {
        DeviceInfo deviceInfo = m_list.get(i);
        deviceInfoViewHolder.m_textView.setText(deviceInfo.getM_name());
        deviceInfoViewHolder.m_textView2.setText(deviceInfo.getM_deviceId());
        //deviceInfoViewHolder.m_cbx.setChecked(true);
        if(deviceInfo.getM_state() == 1)
        {
            deviceInfoViewHolder.m_imageView.setImageResource(R.drawable.device3);
        }
        if(m_onItemClickListener != null)
        {
            deviceInfoViewHolder.itemView.setOnClickListener(v -> m_onItemClickListener.OnClick(i));
            deviceInfoViewHolder.m_textView2.setOnClickListener(v -> m_onClickListener.OnClick(i));
            deviceInfoViewHolder.itemView.setOnLongClickListener(v ->
            {
                m_onItemClickListener.OnLongClick(i);
                return false;
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
    class DeviceInfoChooseViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_textView;
        TextView m_textView2;
        ImageView m_imageView;
        CheckBox m_cbx;

        public DeviceInfoChooseViewHolder(@NonNull View itemView)
        {
            super(itemView);
            m_imageView = itemView.findViewById(R.id.img_choose);
            m_textView = itemView.findViewById(R.id.text_view_choose);
            m_textView2 = itemView.findViewById(R.id.text_view2_choose);
            m_cbx = itemView.findViewById(R.id.checkBox);
        }
    }

}