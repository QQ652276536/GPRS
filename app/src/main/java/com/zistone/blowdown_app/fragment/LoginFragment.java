package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zistone.blowdown_app.PropertiesUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.UserSharedPreference;
import com.zistone.blowdown_app.entity.UserInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String TAG = "LoginFragment";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    private static String URL;
    //间隔时间
    private static int TIMEINTERVAL = 5 * 1000;
    //6~12位字母数字组合或6位中文
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})|[\\u4e00-\\u9fa5]{2,6}";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    private String mParam1;
    private String mParam2;
    private Context m_context;
    private View m_userView;
    private EditText m_editText_userName;
    private EditText m_editText_password;
    private Button m_btn_login;
    private Button m_btn_register;
    private Button m_btn_forget;
    private ProgressBar m_progressBar;
    private Timer m_loginTimer;
    private BottomNavigationView m_bottomNavigationView;
    private OnFragmentInteractionListener mListener;

    public static LoginFragment newInstance(String param1, String param2)
    {
        LoginFragment fragment = new LoginFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
            IsLoginEnd();
            switch(message.what)
            {
                case MESSAGE_GETRESPONSE_SUCCESS:
                {
                    String responseStr = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(responseStr, UserInfo.class);
                    LoginResult(userInfo);
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_context, "登录超时,请检查网络环境", Toast.LENGTH_SHORT).show();
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
        new Thread(() ->
        {
            Looper.prepare();
            UserInfo userInfo = new UserInfo();
            userInfo.setM_userName(m_editText_userName.getText().toString());
            userInfo.setM_password(m_editText_password.getText().toString());
            String jsonData = JSON.toJSONString(userInfo);
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
            //创建Post请求的方式
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //Android中不允许任何网络的交互在主线程中进行
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "请求失败:" + e.toString());
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
                    Log.i(TAG, "请求响应:" + responseStr);
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
        }).start();
    }

    /**
     * 登录成功与否
     */
    private void LoginResult(UserInfo userInfo)
    {
        if(userInfo != null)
        {
            Log.i(TAG, "登录成功,用户真实姓名为:" + userInfo.getM_realName());
            //本地存储用户基本信息
            UserSharedPreference.LoginSuccess(m_context, userInfo);
            UserInfoFragment userInfoFragment = UserInfoFragment.newInstance("", "");
            getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userInfoFragment, "userInfoFragment").commitNow();
            //当前碎片切换为设备碎片
            m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(1).getItemId());
        }
        else
        {
            Log.i(TAG, "登录失败:用户名或密码错误");
            Toast.makeText(m_context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 登录完成,用于释放控件
     */
    private void IsLoginEnd()
    {
        m_progressBar.setVisibility(View.INVISIBLE);
        m_editText_userName.setEnabled(true);
        m_editText_password.setEnabled(true);
        m_btn_login.setEnabled(true);
        m_btn_forget.setEnabled(true);
        m_btn_register.setEnabled(true);
    }

    /**
     * 正在登录,用于禁止控件
     */
    private void IsLogining()
    {
        m_progressBar.setVisibility(View.VISIBLE);
        m_editText_userName.setEnabled(false);
        m_editText_password.setEnabled(false);
        m_btn_login.setEnabled(false);
        m_btn_forget.setEnabled(false);
        m_btn_register.setEnabled(false);
    }

    private void Login()
    {
        m_loginTimer = new Timer();
        TIMEINTERVAL = 5 * 1000;
        IsLogining();
        SendWithOkHttp();
        TimerTask loginTask = new TimerTask()
        {
            @Override
            public void run()
            {
                //返回到UI线程,两种更新UI的方法之一
                getActivity().runOnUiThread(() ->
                {
                    IsLoginEnd();
                    //从任务队列中取消任务
                    m_loginTimer.cancel();
                    Toast.makeText(m_context, "登录失败,请检查网络", Toast.LENGTH_SHORT).show();
                });
            }
        };
        //请求超时已由Http请求设置,该定时器已弃用
        //任务、延迟执行时间
        //m_loginTimer.schedule(loginTask, TIMEINTERVAL);
    }

    private void InitView()
    {
        m_context = m_userView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/UserInfo/Login";
        m_editText_userName = m_userView.findViewById(R.id.editTextUserName_login);
        m_editText_userName.setOnFocusChangeListener(this);
        m_editText_password = m_userView.findViewById(R.id.editTextPassword_login);
        m_editText_password.setOnFocusChangeListener(this);
        m_btn_login = m_userView.findViewById(R.id.btn_login_login);
        m_btn_login.setOnClickListener(this);
        m_btn_register = m_userView.findViewById(R.id.btn_register_login);
        m_btn_register.setOnClickListener(this);
        m_btn_forget = m_userView.findViewById(R.id.btn_forget_login);
        m_btn_forget.setOnClickListener(this);
        m_progressBar = m_userView.findViewById(R.id.progressBar_login);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        m_bottomNavigationView = getActivity().findViewById(R.id.nav_view);
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
        //m_loginTimer.cancel();
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
        m_userView = inflater.inflate(R.layout.fragment_user_login, container, false);
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

    @Override
    public void onClick(View v)
    {
        //隐藏键盘
        InputMethodManager inputMethodManager = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isActive())
        {
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        }
        //登录,这里是发起登录请求
        if(R.id.btn_login_login == v.getId())
        {
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
        //注册,这里是跳转至注册页面
        else if(R.id.btn_register_login == v.getId())
        {
            RegisterFragment registerFragment = RegisterFragment.newInstance("", "");
            getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, registerFragment, "registerFragment").commitNow();

        }
        //忘记密码,这里是跳转至找回密码页
        else if(R.id.btn_forget_login == v.getId())
        {
            ForgetFragment forgetFragment = ForgetFragment.newInstance("", "");
            getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, forgetFragment, "forgetFragment").commitNow();
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(R.id.editTextUserName_login == v.getId())
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
        if(R.id.editTextPassword_login == v.getId())
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

}
