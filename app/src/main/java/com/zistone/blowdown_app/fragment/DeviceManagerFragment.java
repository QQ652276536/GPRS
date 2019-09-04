package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blowdown_app.PropertiesUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.DeviceInfoRecyclerAdapter;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.http.OkHttpUtil;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeviceManagerFragment extends Fragment implements View.OnClickListener
{
    private Context m_context;
    private View m_deviceManagerView;
    private OnFragmentInteractionListener mListener;

    @Override
    public void onClick(View v)
    {
        if(R.id.btn_canuse_manager == v.getId())
        {
        }
        else if(R.id.btn_cannot_use_manager == v.getId())
        {
        }
        else if(R.id.btn_add_manager == v.getId())
        {
        }
    }

    public static DeviceManagerFragment newInstance()
    {
        DeviceManagerFragment tempFragment = new DeviceManagerFragment();
        return tempFragment;
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = m_deviceManagerView.getContext();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
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
        m_deviceManagerView = inflater.inflate(R.layout.fragment_device_manager, container, false);
        InitView();
        return m_deviceManagerView;
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
