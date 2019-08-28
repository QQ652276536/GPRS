package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blowdown_app.ImageUtil;
import com.zistone.blowdown_app.PropertiesUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.UserSharedPreference;
import com.zistone.blowdown_app.entity.UserInfo;
import com.zistone.blowdown_app.http.OkHttpUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    //6~12位字母数字组合
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";

    private static String URL;

    private String mParam1;
    private String mParam2;

    private Context m_context;
    private View m_registerView;
    private EditText m_editText_userName;
    private EditText m_editText_userRealName;
    private EditText m_editText_userPhone;
    private EditText m_editText_password;
    private EditText m_editText_rePassword;
    private ImageButton m_btnReturn;
    private Button m_btnRegister;
    private ProgressBar m_registerProgressBar;

    private OnFragmentInteractionListener mListener;

    public RegisterFragment()
    {
    }

    public static RegisterFragment newInstance(String param1, String param2)
    {
        RegisterFragment fragment = new RegisterFragment();
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
        m_registerView = inflater.inflate(R.layout.fragment_register, container, false);
        InitView();
        return m_registerView;
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

    @Override
    public void onClick(View v)
    {
        //登录,这里是跳转至登录页面
        if(R.id.btn_return_register == v.getId())
        {
            List<Fragment> fragmentList = getFragmentManager().getFragments();
            for(Fragment fragment : fragmentList)
            {
                //注意:一个FragmentTransaction只能Commit一次,不要用全局或共享一个FragmentTransaction对象,多个Fragment则多次get
                if(!"loginFragment".equals(fragment.getTag()))
                {
                    getFragmentManager().beginTransaction().hide(fragment).commitNow();
                }
                else
                {
                    getFragmentManager().beginTransaction().show(fragment).commitNow();
                }
            }
        }
        //注册,这里是发起注册请求
        else if(R.id.btn_register_register == v.getId())
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
                Register();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(R.id.editTextUserName_register == v.getId())
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
        if(R.id.editTextPassword_register == v.getId())
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

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            IsRegisterEd();
            switch(message.what)
            {
                case MESSAGE_GETRESPONSE_SUCCESS:
                {
                    String responseStr = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(responseStr, UserInfo.class);
                    RegisterResult(userInfo);
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_context, "注册超时,请检查网络环境", Toast.LENGTH_SHORT).show();
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
                UserInfo userInfo = new UserInfo();
                userInfo.setM_realName(m_editText_userRealName.getText().toString());
                userInfo.setM_userName(m_editText_userName.getText().toString());
                userInfo.setM_phoneNumber(m_editText_userPhone.getText().toString());
                userInfo.setM_password(m_editText_rePassword.getText().toString());
                OkHttpUtil okHttpUtil = new OkHttpUtil();
                //异步方式发起请求,回调处理信息
                okHttpUtil.AsynSendByPost(URL, userInfo, new Callback()
                {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e)
                    {
                        Log.e("RegisterFragment", "请求失败:" + e.toString());
                        Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_FAIL, "请求失败:" + e.toString());
                        handler.sendMessage(message);
                    }

                    //获得请求响应的字符串:response.body().string()该方法只能被调用一次!另:toString()返回的是对象地址
                    //获得请求响应的二进制字节数组:response.body().bytes()
                    //获得请求响应的inputStream:response.body().byteStream()
                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                    {
                        String responseStr = response.body().string();
                        Log.i("RegisterFragment", "请求响应:" + responseStr);
                        if(response.isSuccessful())
                        {
                            Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_SUCCESS, responseStr);
                            handler.sendMessage(message);
                        }
                        else
                        {
                            Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_FAIL, responseStr);
                            handler.sendMessage(message);
                        }
                    }
                });
                Looper.loop();
            }
        }).start();
    }

    /**
     * 注册成功与否
     */
    private void RegisterResult(UserInfo userInfo)
    {
        if(userInfo != null)
        {
            Log.i("RegisterLog", "注册成功:用户真实姓名为:" + userInfo.getM_realName());
            //跳转至登录页面并将用户名填至输入框
            List<Fragment> fragmentList = getFragmentManager().getFragments();
            for(Fragment fragment : fragmentList)
            {
                if(!"loginFragment".equals(fragment.getTag()))
                {
                    getFragmentManager().beginTransaction().hide(fragment).commitNow();
                }
                else
                {
                    ((LoginFragment) fragment).m_editText_userName.setText(userInfo.getM_userName());
                    getFragmentManager().beginTransaction().show(fragment).commitNow();
                }
            }
        }
        else
        {
            Log.i("RegisterLog", "注册失败:用户名已被注册");
            Toast.makeText(m_context, "注册失败:用户名已被注册", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 注册完成,用于释放控件
     */
    private void IsRegisterEd()
    {
        m_registerProgressBar.setVisibility(View.INVISIBLE);
        m_editText_userName.setEnabled(true);
        m_editText_userRealName.setEnabled(true);
        m_editText_userPhone.setEnabled(true);
        m_editText_password.setEnabled(true);
        m_editText_rePassword.setEnabled(true);
        m_btnReturn.setEnabled(true);
        m_btnRegister.setEnabled(true);
    }

    /**
     * 正在注册,用于禁止控件
     */
    private void IsRegistering()
    {
        m_registerProgressBar.setVisibility(View.VISIBLE);
        m_editText_userName.setEnabled(false);
        m_editText_userRealName.setEnabled(false);
        m_editText_userPhone.setEnabled(false);
        m_editText_password.setEnabled(false);
        m_editText_rePassword.setEnabled(false);
        //m_btnReturn.setEnabled(false);
        m_btnRegister.setEnabled(false);
    }

    private void Register()
    {
        IsRegistering();
        SendWithOkHttp();
    }

    private void InitView()
    {
        m_context = m_registerView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/UserInfo/Register";
        m_editText_userName = m_registerView.findViewById(R.id.editTextUserName_register);
        m_editText_userName.setOnFocusChangeListener(this);
        m_editText_userRealName = m_registerView.findViewById(R.id.editTextRealName_register);
        m_editText_userRealName.setOnFocusChangeListener(this);
        m_editText_userPhone = m_registerView.findViewById(R.id.editTextPhone_register);
        m_editText_userPhone.setOnFocusChangeListener(this);
        m_editText_password = m_registerView.findViewById(R.id.editTextPassword_register);
        m_editText_password.setOnFocusChangeListener(this);
        m_editText_rePassword = m_registerView.findViewById(R.id.editTextRePassword_register);
        m_editText_rePassword.setOnFocusChangeListener(this);
        m_btnReturn = m_registerView.findViewById(R.id.btn_return_register);
        m_btnReturn.setOnClickListener(this);
        m_btnRegister = m_registerView.findViewById(R.id.btn_register_register);
        m_btnRegister.setOnClickListener(this);
        m_registerProgressBar = m_registerView.findViewById(R.id.progressBar_register);
    }
}
