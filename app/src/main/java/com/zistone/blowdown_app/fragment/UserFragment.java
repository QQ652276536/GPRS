package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.zistone.blowdown_app.ImageUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.UserSharedPreference;

import java.util.List;

public class UserFragment extends Fragment
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private Context m_context;
    private View m_userView;
    private LoginFragment m_loginFragment;
    private RegisterFragment m_registerFragment;
    private ForgetFragment m_forgetFragment;
    private UserInfoFragment m_userInfoFragment;

    private OnFragmentInteractionListener mListener;

    public UserFragment()
    {
    }

    public static UserFragment newInstance(String param1, String param2)
    {
        UserFragment fragment = new UserFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void InitView()
    {
        m_context = getContext();
        //注意:一个FragmentTransaction只能Commit一次,不要用全局或共享一个FragmentTransaction对象,多个Fragment则多次get
        //用户信息
        if(m_userInfoFragment == null)
        {
            m_userInfoFragment = UserInfoFragment.newInstance("", "");
        }
        if(!m_userInfoFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_userInfoFragment, "userInfoFragment").commitNow();
        }
        //登录
        if(m_loginFragment == null)
        {
            m_loginFragment = LoginFragment.newInstance("", "");
        }
        if(!m_loginFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_loginFragment, "loginFragment").commitNow();
        }
        //注册
        if(m_registerFragment == null)
        {
            m_registerFragment = RegisterFragment.newInstance("", "");
        }
        if(!m_registerFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_registerFragment, "registerFragment").commitNow();
        }
        //找回密码
        if(m_forgetFragment == null)
        {
            m_forgetFragment = ForgetFragment.newInstance("", "");
        }
        if(!m_forgetFragment.isAdded())
        {
            getChildFragmentManager().beginTransaction().add(R.id.fragment_current_user, m_forgetFragment, "forgetFragment").commitNow();
        }
        List<Fragment> fragmentList = getChildFragmentManager().getFragments();
        //已经登录过则显示用户信息页面,否则显示登录页面
        String realName = UserSharedPreference.GetRealName(m_context);
        int state = UserSharedPreference.GetState(m_context);
        if(!"".equals(realName) && 1 == state)
        {
            for(Fragment fragment : fragmentList)
            {
                if(!"userInfoFragment".equals(fragment.getTag()))
                {
                    getChildFragmentManager().beginTransaction().hide(fragment).commitNow();
                }
                else
                {
                    getChildFragmentManager().beginTransaction().show(fragment).commitNow();
                }
            }
        }
        else
        {
            for(Fragment fragment : fragmentList)
            {
                if(!"loginFragment".equals(fragment.getTag()))
                //if(!"userInfoFragment".equals(fragment.getTag()))
                {
                    getChildFragmentManager().beginTransaction().hide(fragment).commitNow();
                }
                else
                {
                    getChildFragmentManager().beginTransaction().show(fragment).commitNow();
                }
            }
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
        InitView();
        return m_userView;
    }

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
