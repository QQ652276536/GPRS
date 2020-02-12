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

public class DeviceInfoChooseRecyclerAdapter extends RecyclerView.Adapter<DeviceInfoChooseRecyclerAdapter.DeviceInfoChooseViewHolder>
{
    private static final String TAG = "DeviceInfoRecyclerAdapt";
    private Context _context;
    private List<DeviceInfo> _list;
    private View _inflater;
    private OnItemClickListener _onItemClickListener;
    private OnClickListener _onClickListener;

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
        _context = context;
        _list = list;
    }

    public void SetOnClickListener(OnClickListener onClickListener)
    {
        this._onClickListener = onClickListener;
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
    public DeviceInfoChooseViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i)
    {
        _inflater = LayoutInflater.from(_context).inflate(R.layout.device_item_choose, viewGroup, false);
        DeviceInfoChooseViewHolder deviceInfoChooseViewHolder = new DeviceInfoChooseViewHolder(_inflater);
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
        DeviceInfo deviceInfo = _list.get(i);
        deviceInfoViewHolder._txtView.setText(deviceInfo.getName());
        deviceInfoViewHolder._txtView2.setText(deviceInfo.getDeviceId());
        //deviceInfoViewHolder._cbx.setChecked(true);
        if(deviceInfo.getState() == 1)
        {
            deviceInfoViewHolder._imageView.setImageResource(R.drawable.device3);
        }
        if(_onItemClickListener != null)
        {
            deviceInfoViewHolder.itemView.setOnClickListener(v -> _onItemClickListener.OnClick(i));
            deviceInfoViewHolder._txtView2.setOnClickListener(v -> _onClickListener.OnClick(i));
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
    class DeviceInfoChooseViewHolder extends RecyclerView.ViewHolder
    {
        TextView _txtView;
        TextView _txtView2;
        ImageView _imageView;

        public DeviceInfoChooseViewHolder(@NonNull View itemView)
        {
            super(itemView);
            _imageView = itemView.findViewById(R.id.img_choose);
            _txtView = itemView.findViewById(R.id.text_view_choose);
            _txtView2 = itemView.findViewById(R.id.text_view2_choose);
        }
    }

}