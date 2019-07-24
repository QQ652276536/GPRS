package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.blowdown_app.R;

public class UserFragment extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View m_userView;
    //登录页
    private LoginFragment m_loginFragment;
    private RegisterFragment m_registerFragment;
    private ForgetFragment m_forgetFragment;

    private OnFragmentInteractionListener mListener;

    public UserFragment()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1, String param2)
    {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void InitData()
    {
        //子碎片不在管理器中则添加进去
        if(m_loginFragment == null)
        {
            m_loginFragment = LoginFragment.newInstance("", "");
        }
        //这里注意子Fragment的add顺序,后面在切换子Fragment时会用到
        if(!m_loginFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_loginFragment).commitAllowingStateLoss();
        }
        //用户碎片的默认子碎片为登录
        else
        {
            getChildFragmentManager().beginTransaction().show(m_loginFragment).commitAllowingStateLoss();
        }
        if(m_registerFragment == null)
        {
            m_registerFragment = RegisterFragment.newInstance("", "");
        }
        if(!m_registerFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_registerFragment).commitAllowingStateLoss();
        }
        if(m_forgetFragment == null)
        {
            m_forgetFragment = ForgetFragment.newInstance("", "");
        }
        if(!m_forgetFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_forgetFragment).commitAllowingStateLoss();
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * 创建Fragment时回调,只会被调用一次
     *
     * @param savedInstanceState
     */
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

    /**
     * 绘制Fragment组件时回调
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_userView = inflater.inflate(R.layout.fragment_user, container, false);
        InitData();
        return m_userView;
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

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

}
