package com.example.blowdown_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.blowdown_app.entity.UserInfo;
import com.example.blowdown_app.http.HttpClientUtil;
import com.google.gson.Gson;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class UserFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL = "http://10.0.2.2:8080/Blowdown/UserInfo/Login";
    private static final int SHOW_RESPONSE = 0;
    //6~12位字母数字组合
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";

    private String mParam1;
    private String mParam2;

    private View m_userView;
    private EditText m_editText_userName;
    private EditText m_editText_password;
    private Button m_btn_login;
    private Button m_btn_register;
    private Button m_btn_forget;
    private ProgressBar m_loginProgressBar;

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

    @Override
    public void onClick(View v)
    {
        if(R.id.btn_login == v.getId())
        {
            //隐藏键盘
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm.isActive())
            {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
            if(Pattern.matches(REGEXUSERNAME, m_editText_userName.getText().toString()))
            {
                m_editText_userName.setError(null);
            }
            else
            {
                m_editText_userName.setError("用户名非法");
            }
            if(Pattern.matches(REGEXPASSWORD, m_editText_password.getText().toString()))
            {
                m_editText_password.setError(null);
            }
            else
            {
                m_editText_password.setError("密码非法");
            }
            if(m_editText_userName.getError() == null && m_editText_password.getError() == null)
            {
                Login();
            }
        }
        else if(R.id.btn_register == v.getId())
        {
        }
        else if(R.id.btn_forget == v.getId())
        {
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(R.id.editTextUserName == v.getId())
        {
            if(hasFocus)
            {
                m_editText_userName.setError(null);
            }
            else
            {
                if(Pattern.matches(REGEXUSERNAME, m_editText_userName.getText().toString()))
                {
                    m_editText_userName.setError(null);
                }
                else
                {
                    m_editText_userName.setError("用户名非法");
                }
            }
        }
        if(R.id.editTextPasswrod == v.getId())
        {
            if(hasFocus)
            {
                m_editText_password.setError(null);
            }
            else
            {
                if(Pattern.matches(REGEXPASSWORD, m_editText_password.getText().toString()))
                {
                    m_editText_password.setError(null);
                }
                else
                {
                    m_editText_password.setError("密码非法");
                }
            }
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    private void IsLogining()
    {
        m_loginProgressBar.setVisibility(View.VISIBLE);
        m_editText_userName.setEnabled(false);
        m_editText_password.setEnabled(false);
        m_btn_login.setEnabled(false);
        m_btn_forget.setEnabled(false);
        m_btn_register.setEnabled(false);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch(message.what)
            {
                case SHOW_RESPONSE:
                    String responseStr = (String) message.obj;
                    Gson gson = new Gson();
                    UserInfo userInfo = gson.fromJson(responseStr, UserInfo.class);
                    //登录成功
                    if(userInfo != null)
                    {
                        UserSharedPreference.SetUserName(m_userView.getContext(), userInfo.getM_userName());
                        UserSharedPreference.SetPassword(m_userView.getContext(), userInfo.getM_password());
                        UserSharedPreference.SetRealName(m_userView.getContext(), userInfo.getM_realName());
                        UserSharedPreference.SetLevel(m_userView.getContext(), userInfo.getM_level());
                        IsLoginEd();
                    }
                    else
                    {
                        Toast.makeText(m_userView.getContext(), "登录失败,请检查网络", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    /**
     * 发送网络请求,在里面开启线程
     */
    private void SendWithHttpClient()
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Map<String, String> map = new HashMap<>();
                map.put("m_userName", m_editText_userName.getText().toString());
                map.put("m_password", m_editText_password.getText().toString());
                HttpClientUtil httpClientUtil = new HttpClientUtil();
                String responseStr = httpClientUtil.SendByPost(URL, map);
                Message message = new Message();
                message.what = SHOW_RESPONSE;
                message.obj = responseStr;
                handler.sendMessage(message);
            }
        }).start();
    }

    private void IsLoginEd()
    {
        m_loginProgressBar.setVisibility(View.INVISIBLE);
        m_editText_userName.setEnabled(true);
        m_editText_password.setEnabled(true);
        m_btn_login.setEnabled(true);
        m_btn_forget.setEnabled(true);
        m_btn_register.setEnabled(true);
        //TODO:跳转至设备页面
    }

    private void Login()
    {
        IsLogining();
        SendWithHttpClient();
    }

    private void InitData()
    {
        m_editText_userName = m_userView.findViewById(R.id.editTextUserName);
        m_editText_userName.setOnFocusChangeListener(this);
        m_editText_password = m_userView.findViewById(R.id.editTextPasswrod);
        m_editText_password.setOnFocusChangeListener(this);
        m_btn_login = m_userView.findViewById(R.id.btn_login);
        m_btn_login.setOnClickListener(this);
        m_btn_register = m_userView.findViewById(R.id.btn_register);
        m_btn_register.setOnClickListener(this);
        m_btn_forget = m_userView.findViewById(R.id.btn_forget);
        m_btn_forget.setOnClickListener(this);
        m_loginProgressBar = m_userView.findViewById(R.id.progressBar);
    }
}
