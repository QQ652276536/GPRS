package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.UserInfo;

import java.util.regex.Pattern;

public class UserInfoFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL = "http://192.168.10.197:8080/Blowdown_Web/UserInfo/Logout";
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    //6~12位字母数字组合
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";

    private String mParam1;
    private String mParam2;

    private Context m_context;
    private View m_userInfoView;
    private EditText m_editText_userRealName;
    private EditText m_editText_userPhone;
    private EditText m_editText_password;
    private EditText m_editText_rePassword;
    private Button m_btnUpdate;
    private Button m_btnLogout;
    private ProgressBar m_updateUserInfoProgressBar;
    private ImageView m_imageView;

    private OnFragmentInteractionListener mListener;

    public UserInfoFragment()
    {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserInfoFragment newInstance(String param1, String param2)
    {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        m_userInfoView = inflater.inflate(R.layout.fragment_userinfo, container, false);
        InitView();
        return m_userInfoView;
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

    @Override
    public void onClick(View v)
    {
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(R.id.editText_userRealName_userInfo == v.getId())
        {
            if(hasFocus)
            {
                m_editText_userRealName.setError(null);
            }
            else
            {
                if(Pattern.matches(REGEXUSERNAME, m_editText_userRealName.getText().toString()))
                {
                    m_editText_userRealName.setError(null);
                }
                else
                {
                    m_editText_userRealName.setError("姓名非法");
                }
            }
        }
        else if(R.id.editText_userPhone_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
        else if(R.id.editText_password_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
        else if(R.id.editText_rePassword_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch(message.what)
            {
                case MESSAGE_GETRESPONSE_SUCCESS:
                {
                    String responseStr = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(responseStr, UserInfo.class);
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_context, "请求超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 用OkHttp发送网络请求,并在里面开启线程
     */
    private void SendWithOkHttp()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();

                Looper.loop();
            }
        }).start();
    }

    private void InitView()
    {
        m_context = m_userInfoView.getContext();
        m_editText_userRealName = m_userInfoView.findViewById(R.id.editText_userRealName_userInfo);
        m_editText_userRealName.setOnFocusChangeListener(this);
        m_editText_userPhone = m_userInfoView.findViewById(R.id.editText_userPhone_userInfo);
        m_editText_userPhone.setOnFocusChangeListener(this);
        m_editText_password = m_userInfoView.findViewById(R.id.editText_password_userInfo);
        m_editText_password.setOnFocusChangeListener(this);
        m_editText_rePassword = m_userInfoView.findViewById(R.id.editText_rePassword_userInfo);
        m_editText_rePassword.setOnFocusChangeListener(this);
        m_btnUpdate = m_userInfoView.findViewById(R.id.btnUpdate_userInfo);
        m_btnUpdate.setOnClickListener(this);
        m_btnLogout = m_userInfoView.findViewById(R.id.btnLogout_userInfo);
        m_btnLogout.setOnClickListener(this);
        m_imageView = m_userInfoView.findViewById(R.id.imageView);
        m_imageView.setOnClickListener(this);
        m_updateUserInfoProgressBar = m_userInfoView.findViewById(R.id.progressBar_updateUserInfo);
    }
}
