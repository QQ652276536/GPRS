package com.zistone.gprs.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zistone.gprs.R;

public class DeviceFragment_Manage extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceFragment_Manage";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Context m_context;
    private View m_deviceView;
    private Button m_btn_canUse;
    private Button m_btn_notUse;
    private Button m_btn_add;
    private OnFragmentInteractionListener mListener;

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.btn_canuse_manager:
            {
                DeviceFragment_List deviceFragment_list = DeviceFragment_List.newInstance(1, "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_list, "deviceFragment_list").commitNow();
                break;
            }
            case R.id.btn_not_use_manager:
            {
                DeviceFragment_List deviceFragment_list = DeviceFragment_List.newInstance(0, "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_list, "deviceFragment_list").commitNow();
                break;
            }
            case R.id.btn_add_manager:
            {
                DeviceFragment_Add deviceFragment_add = DeviceFragment_Add.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_add, "deviceFragment_add").commitNow();
                break;
            }
        }
    }

    public static DeviceFragment_Manage newInstance(String param1, String param2)
    {
        DeviceFragment_Manage fragment = new DeviceFragment_Manage();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
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
        if (getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceView = inflater.inflate(R.layout.fragment_device_manage, container, false);

        m_context = m_deviceView.getContext();
        m_btn_canUse = m_deviceView.findViewById(R.id.btn_canuse_manager);
        m_btn_canUse.setOnClickListener(this);
        m_btn_notUse = m_deviceView.findViewById(R.id.btn_not_use_manager);
        m_btn_notUse.setOnClickListener(this);
        m_btn_add = m_deviceView.findViewById(R.id.btn_add_manager);
        m_btn_add.setOnClickListener(this);
        return m_deviceView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
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
