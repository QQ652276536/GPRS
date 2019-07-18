package com.zistone.blowdown_app.control;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zistone.blowdown_app.R;

import java.util.List;

public class LeftSlideRemoveAdapter extends RecyclerView.Adapter<LeftSlideRemoveAdapter.ViewHolder>
{

    private List<String> m_listStr;
    private Context m_context;
    private LayoutInflater m_layoutInflater;

    public LeftSlideRemoveAdapter(List<String> list, Context context)
    {
        this.m_listStr = list;
        this.m_context = context;
        this.m_layoutInflater = LayoutInflater.from(context);
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView m_textView;
        ImageView m_image;
        LinearLayout m_linear_item, m_linear_hidden;

        public ViewHolder(View itemView)
        {
            super(itemView);
            m_textView = itemView.findViewById(R.id.text_title);
            m_image = itemView.findViewById(R.id.img);
            m_linear_item = itemView.findViewById(R.id.linear_item);
            m_linear_hidden = itemView.findViewById(R.id.linear_hidden);
        }
    }

    @Override
    public LeftSlideRemoveAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return new ViewHolder(m_layoutInflater.inflate(R.layout.view_left_slide_remove, parent, false));
    }

    @Override
    public void onBindViewHolder(LeftSlideRemoveAdapter.ViewHolder holder, int position)
    {
        holder.m_textView.setText(m_listStr.get(position));
        holder.m_image.setImageResource(R.mipmap.device3);
    }

    @Override
    public int getItemCount()
    {
        return m_listStr == null ? 0 : m_listStr.size();
    }
}
