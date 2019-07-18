package com.zistone.blowdown_app.fragment;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.LeftSlideRemoveAdapter;
import com.zistone.blowdown_app.control.LeftSlideRemoveView;
import com.zistone.blowdown_app.control.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeviceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeviceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceFragment extends Fragment
{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private Context m_context;
    private View m_deviceView;
    private LeftSlideRemoveView m_leftSlideRemoveView;

    private String[] titles = {
            "设备A\n00000000001", "设备B\n00000000001", "设备C\n00000000001", "设备D\n00000000001", "设备E\n00000000001", "设备F\n00000000001"
            ,"设备G\n00000000001", "设备H\n00000000001", "设备I\n00000000001"
    };
    private List<String> m_listStr = new ArrayList<>();
    private ItemTouchHelper mItemTouchHelper;
    private LeftSlideRemoveAdapter m_leftSlideRemoveAdapter;

    private OnFragmentInteractionListener mListener;

    public DeviceFragment()
    {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceFragment newInstance(String param1, String param2)
    {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void InitView()
    {
        m_context = m_deviceView.getContext();
        m_leftSlideRemoveView = m_deviceView.findViewById(R.id.recyclerView);
        m_leftSlideRemoveView.setLayoutManager(new LinearLayoutManager(getContext()));
        //添加自定义分割线
        m_leftSlideRemoveView.addItemDecoration(new DividerItemDecoration(m_context, DividerItemDecoration.VERTICAL));
        m_leftSlideRemoveAdapter = new LeftSlideRemoveAdapter(m_listStr, getContext());
        m_leftSlideRemoveView.setAdapter(m_leftSlideRemoveAdapter);
        m_leftSlideRemoveView.addOnItemTouchListener(new OnRecyclerItemClickListener(m_leftSlideRemoveView)
        {
            @Override
            public void OnItemClick(RecyclerView.ViewHolder vh)
            {
                Toast.makeText(m_context, m_listStr.get(vh.getLayoutPosition()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnItemLongClick(RecyclerView.ViewHolder vh)
            {
                //判断被拖拽的是否是前两个，如果不是则执行拖拽
                if(vh.getLayoutPosition() != 0 && vh.getLayoutPosition() != 1)
                {
                    mItemTouchHelper.startDrag(vh);
                    //TODO:获取系统震动服务
                }
            }
        });

        mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback()
        {
            /**
             * 是否处理滑动事件 以及拖拽和滑动的方向 如果是列表类型的RecyclerView的只存在UP和DOWN，如果是网格类RecyclerView则还应该多有LEFT和RIGHT
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
                //得到当拖拽的viewHolder的Position
                int fromPosition = viewHolder.getAdapterPosition();
                //拿到当前拖拽到的item的viewHolder
                int toPosition = target.getAdapterPosition();
                if(fromPosition < toPosition)
                {
                    for(int i = fromPosition; i < toPosition; i++)
                    {
                        Collections.swap(m_listStr, i, i + 1);
                    }
                }
                else
                {
                    for(int i = fromPosition; i > toPosition; i--)
                    {
                        Collections.swap(m_listStr, i, i - 1);
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

        mItemTouchHelper.attachToRecyclerView(m_leftSlideRemoveView);

        m_leftSlideRemoveView.m_rightListener = new LeftSlideRemoveView.OnRightClickListener()
        {
            @Override
            public void onRightClick(int position, String id)
            {
                m_listStr.remove(position);
                m_leftSlideRemoveAdapter.notifyDataSetChanged();
                Toast.makeText(m_context, " position = " + position, Toast.LENGTH_SHORT).show();
            }
        };
    }

    public void InitData()
    {
        for(int i = 0; i < titles.length; i++)
        {
            m_listStr.add(titles[i]);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceView = inflater.inflate(R.layout.fragment_device, container, false);
        InitView();
        InitData();
        return m_deviceView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener
    {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
