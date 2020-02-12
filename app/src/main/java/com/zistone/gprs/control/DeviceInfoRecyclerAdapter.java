package com.zistone.gprs.control;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;

import java.util.List;

public class DeviceInfoRecyclerAdapter extends RecyclerView.Adapter<DeviceInfoRecyclerAdapter.DeviceInfoViewHolder>
{
    private static final String TAG = "DeviceInfoRecyclerAdapt";
    private Context _context;
    private List<DeviceInfo> _list;
    private View _inflater;
    private OnItemClickListener _onItemClickListener;

    public interface OnItemClickListener
    {
        void OnClick(int position);

        void OnLongClick(int position);
    }

    public DeviceInfoRecyclerAdapter(Context context, List<DeviceInfo> list)
    {
        _context = context;
        _list = list;
    }

    public void SetOnItemClickListener(OnItemClickListener onItemClickListener)
    {
        this._onItemClickListener = onItemClickListener;
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
        _inflater = LayoutInflater.from(_context).inflate(R.layout.device_item, viewGroup, false);
        DeviceInfoViewHolder deviceInfoViewHolder = new DeviceInfoViewHolder(_inflater);
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
        DeviceInfo deviceInfo = _list.get(i);
        deviceInfoViewHolder._txtView.setText(deviceInfo.getName() + "\n" + deviceInfo.getType());
        if(deviceInfo.getState() == 1)
        {
            deviceInfoViewHolder._imageView.setImageResource(R.drawable.device3);
            deviceInfoViewHolder._txtView.setTextColor(_context.getResources().getColor(R.color.colorPrimary));
        }
        else
        {
            deviceInfoViewHolder._imageView.setImageResource(R.drawable.device4);
        }
        if(_onItemClickListener != null)
        {
            deviceInfoViewHolder.itemView.setOnClickListener(v -> _onItemClickListener.OnClick(i));
            deviceInfoViewHolder.itemView.setOnLongClickListener(v ->
            {
                _onItemClickListener.OnLongClick(i);
                return false;
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return _list.size();
    }

    /**
     * 内部类,用来绑定控件
     */
    class DeviceInfoViewHolder extends RecyclerView.ViewHolder
    {
        TextView _txtView;
        ImageView _imageView;

        public DeviceInfoViewHolder(@NonNull View itemView)
        {
            super(itemView);
            _txtView = itemView.findViewById(R.id.text_view);
            _imageView = itemView.findViewById(R.id.img);
        }
    }

}