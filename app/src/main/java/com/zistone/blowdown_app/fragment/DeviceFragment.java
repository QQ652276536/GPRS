package com.zistone.blowdown_app.fragment;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zistone.blowdown_app.MainActivity;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.LeftSlideRemoveAdapter;
import com.zistone.blowdown_app.control.LeftSlideRemoveView;
import com.zistone.blowdown_app.control.OnRecyclerItemClickListener;
import com.zistone.blowdown_app.entity.DeviceInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DeviceFragment extends Fragment
{
    private String[] titles = {
            "设备A", "设备B", "设备C", "设备D", "设备E", "设备F", "设备G", "设备H"
    };
    private String[] types = {
            "A00001", "B00001", "C00001", "D00001", "E00001", "F00001", "G00001", "H00001"
    };
    private int[] states = {
            1, 0, 0, 1, 1, 0, 0, 1
    };
    private double[] lats = {
            22.52837, 31.245105, 37.023537, 30.66441, 31.200547, 0, 0, 0
    };
    private double[] lots = {
            114.003481, 121.506377, 116.289429, 111.377191, 121.326997, 0, 0, 0
    };

    private Context m_context;
    private View m_deviceView;
    private LeftSlideRemoveView m_leftSlideRemoveView;
    private List<DeviceInfo> m_deviceList = new ArrayList<>();
    private ItemTouchHelper m_itemTouchHelper;
    private LeftSlideRemoveAdapter m_leftSlideRemoveAdapter;
    private OnFragmentInteractionListener mListener;
    private MainActivity m_mainActivity;

    public DeviceFragment()
    {
    }

    /**
     * 使用工厂方法创建新的实例
     *
     * @param activity
     * @param param2
     * @return
     */
    public static DeviceFragment newInstance(MainActivity activity, String param2)
    {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putSerializable("MainActivityClass", activity);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = m_deviceView.getContext();
        m_mainActivity = (MainActivity) getArguments().getSerializable("MainActivityClass");
        m_leftSlideRemoveView = m_deviceView.findViewById(R.id.recyclerView);
        m_leftSlideRemoveView.setLayoutManager(new LinearLayoutManager(getContext()));
        //添加自定义分割线
        m_leftSlideRemoveView.addItemDecoration(new DividerItemDecoration(m_context, DividerItemDecoration.VERTICAL));
        m_leftSlideRemoveAdapter = new LeftSlideRemoveAdapter(m_deviceList, getContext());
        m_leftSlideRemoveView.setAdapter(m_leftSlideRemoveAdapter);
        //注册触摸监听事件
        m_leftSlideRemoveView.addOnItemTouchListener(new OnRecyclerItemClickListener(m_leftSlideRemoveView)
        {
            @Override
            public void OnItemClick(RecyclerView.ViewHolder viewHolder)
            {
                DeviceInfo tempDevice = m_deviceList.get(viewHolder.getLayoutPosition());
                Toast.makeText(m_context, tempDevice.getM_deviceName(), Toast.LENGTH_SHORT).show();
                int a = m_mainActivity.getSupportFragmentManager().getFragments().size();
                //重新实例化地图碎片以达到重新加载设备位置
                getFragmentManager().beginTransaction().remove(m_mainActivity.m_mapFragment);
                m_mainActivity.m_mapFragment = null;
                m_mainActivity.m_deviceInfo = tempDevice;
                m_mainActivity.m_bottomNavigationView.setSelectedItemId(m_mainActivity.m_bottomNavigationView.getMenu().getItem(0).getItemId());
            }

            @Override
            public void OnItemLongClick(RecyclerView.ViewHolder vh)
            {
                //判断被拖拽的是否是前两个,如果不是则执行拖拽
                if(vh.getLayoutPosition() != 0 && vh.getLayoutPosition() != 1)
                {
                    m_itemTouchHelper.startDrag(vh);
                    //TODO:获取系统震动服务
                }
            }
        });
        m_itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback()
        {
            /**
             * 是否处理滑动事件以及拖拽和滑动的方向,如果是列表类型的RecyclerView的只存在UP和DOWN,如果是网格类RecyclerView则还应该多有LEFT和RIGHT
             * @param recyclerView
             * @param viewHolder
             * @return
             */
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
            {
                if(recyclerView.getLayoutManager() instanceof GridLayoutManager)
                {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
                else
                {
                    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                    final int swipeFlags = 0;
                    return makeMovementFlags(dragFlags, swipeFlags);
                }
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
            {
                //得到当前拖拽的Item的位置
                int fromPosition = viewHolder.getAdapterPosition();
                //要拖拽到的Item的位置
                int toPosition = target.getAdapterPosition();
                if(fromPosition < toPosition)
                {
                    for(int i = fromPosition; i < toPosition; i++)
                    {
                        Collections.swap(m_deviceList, i, i + 1);
                    }
                }
                else
                {
                    for(int i = fromPosition; i > toPosition; i--)
                    {
                        Collections.swap(m_deviceList, i, i - 1);
                    }
                }
                m_leftSlideRemoveAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
            {
            }

            /**
             * 拖拽可用
             * @return
             */
            @Override
            public boolean isLongPressDragEnabled()
            {
                return false;
            }

            /**
             * 长按选中Item的时候开始调用
             *
             * @param viewHolder
             * @param actionState
             */
            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
            {
                if(actionState != ItemTouchHelper.ACTION_STATE_IDLE)
                {
                    viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
                }
                super.onSelectedChanged(viewHolder, actionState);
            }

            /**
             * 手指松开的时候还原
             * @param recyclerView
             * @param viewHolder
             */
            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder)
            {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(0);
            }
        });
        m_itemTouchHelper.attachToRecyclerView(m_leftSlideRemoveView);
        //隐藏部分(删除)点击事件的具体实现
        m_leftSlideRemoveView.m_rightListener = new LeftSlideRemoveView.OnRightClickListener()
        {
            @Override
            public void OnRightClick(int position, String id)
            {
                m_deviceList.remove(position);
                m_leftSlideRemoveAdapter.notifyDataSetChanged();
                //Toast.makeText(m_context, " position = " + position, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void InitDeviceData()
    {
        for(int i = 0; i < titles.length; i++)
        {
            DeviceInfo tempDevice = new DeviceInfo();
            tempDevice.setM_deviceName(titles[i]);
            tempDevice.setM_deviceType(types[i]);
            tempDevice.setM_state(states[i]);
            tempDevice.setM_lat(lats[i]);
            tempDevice.setM_lot(lots[i]);
            m_deviceList.add(tempDevice);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceView = inflater.inflate(R.layout.fragment_device, container, false);

        InitView();
        InitDeviceData();
        return m_deviceView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
        {
            mListener = (OnFragmentInteractionListener) context;
        }
        else
        {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }
}
