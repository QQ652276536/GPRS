package com.zistone.blowdown_app.control;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.util.List;

/**
 * 自定义Adapter
 */
public abstract class LeftSlideRemoveAdapter extends BaseAdapter
{
    public Context m_context;
    public OnItemRemoveListener m_listener;

    public LeftSlideRemoveAdapter()
    {
    }

    public LeftSlideRemoveAdapter(Context context)
    {
        this.m_context = context;
    }

    @Override
    public final View getView(final int position, View convertView, ViewGroup parent)
    {
        ViewHolder holder;
        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(m_context);
            convertView = inflater.inflate(R.layout.view_left_slide_remove, parent, false);
            holder = new ViewHolder();
            holder.viewContent = convertView.findViewById(R.id.view_content);
            holder.textRemove = convertView.findViewById(R.id.text_remove);
            convertView.setTag(holder);
            //viewChild是实际的界面
            holder.viewChild = getSubView(position, null, parent);
            holder.viewContent.addView(holder.viewChild);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
            getSubView(position, holder.viewChild, parent);
        }
        holder.textRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(m_listener != null)
                {
                    m_listener.onItemRemove(position);
                    notifyDataSetChanged();
                }
            }
        });
        return convertView;
    }

    public abstract View getSubView(int position, View convertView, ViewGroup parent);

    public class ViewHolder
    {
        RelativeLayout viewContent;
        View viewChild;
        View textRemove;
    }

    public interface OnItemRemoveListener
    {
        void onItemRemove(int position);
    }
}
