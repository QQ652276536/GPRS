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

public class DeviceFragment extends Fragment
{
    private static final String TAG = "DeviceFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private Context _context;
    private View _deviceView;
    private OnFragmentInteractionListener _listener;

    public static DeviceFragment newInstance(String param1, String param2)
    {
        DeviceFragment fragment = new DeviceFragment();
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
        if (_listener != null)
        {
            _listener.onFragmentInteraction(uri);
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
        _deviceView = inflater.inflate(R.layout.fragment_device, container, false);

        _context = getContext();
        //注意:一个FragmentTransaction只能Commit一次,不要用全局或共享一个FragmentTransaction对象,多个Fragment则多次get
        DeviceFragment_Manage deviceFragment_manage = DeviceFragment_Manage.newInstance("", "");
        getChildFragmentManager().beginTransaction().add(R.id.fragment_current_device, deviceFragment_manage, "deviceFragment_manage").show(deviceFragment_manage).commitNow();
        return _deviceView;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener)
        {
            _listener = (OnFragmentInteractionListener) context;
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
        _listener = null;
    }
}
