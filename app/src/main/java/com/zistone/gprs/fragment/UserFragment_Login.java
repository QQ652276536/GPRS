package com.zistone.gprs.fragment;

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
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;
import com.zistone.gprs.util.UserSharedPreference;
import com.zistone.gprs.pojo.UserInfo;

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

public class UserFragment_Login extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String TAG = "UserFragment_Login";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    //间隔时间
    private static int TIMEINTERVAL = 5 * 1000;
    //6~12位字母数字组合或6位中文
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{4,12})|[\\u4e00-\\u9fa5]{2,6}";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    private String _param1;
    private String _param2;
    private Context _context;
    private View _userView;
    private EditText _edtUserName;
    private EditText _edtPassword;
    private Button _btnLogin;
    private Button _btnRegister;
    private Button _btnForget;
    private ProgressBar _progressBar;
    private Timer _loginTimer;
    private BottomNavigationView _bottomNavigationView;
    private OnFragmentInteractionListener _listener;

    public static UserFragment_Login newInstance(String param1, String param2)
    {
        UserFragment_Login fragment = new UserFragment_Login();
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
            switch (message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "登录超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(result, UserInfo.class);
                    LoginResult(userInfo);
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "登录失败,请与管理员联系", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void SendWithOkHttp()
    {
        new Thread(() ->
                   {
                       Looper.prepare();
                       UserInfo userInfo = new UserInfo();
                       userInfo.setUserName(_edtUserName.getText().toString());
                       userInfo.setPassword(_edtPassword.getText().toString());
                       String jsonData = JSON.toJSONString(userInfo);
                       //实例化并设置连接超时时间、读取超时时间
                       OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
                       RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
                       Request request = new Request.Builder().post(requestBody).url(URL).build();
                       Call call = okHttpClient.newCall(request);
                       //异步请求
                       call.enqueue(new Callback()
                       {
                           @Override
                           public void onFailure(@NotNull Call call, @NotNull IOException e)
                           {
                               Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                               handler.sendMessage(message);
                           }

                           //获得请求响应的字符串:response.body().string()该方法只能被调用一次!另:toString()返回的是对象地址
                           //获得请求响应的二进制字节数组:response.body().bytes()
                           //获得请求响应的inputStream:response.body().byteStream()
                           @Override
                           public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                           {
                               String result = response.body().string();
                               if (response.isSuccessful())
                               {
                                   Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                                   handler.sendMessage(message);
                               }
                               else
                               {
                                   Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, result);
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
        if (userInfo != null)
        {
            Log.i(TAG, "登录成功,用户真实姓名为:" + userInfo.getRealName());
            //本地存储用户基本信息
            UserSharedPreference.LoginSuccess(_context, userInfo);
            UserFragment_Info userFragment_info = UserFragment_Info.newInstance("", "");
            getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_info, "userFragment_info").commitNow();
            //当前碎片切换为设备碎片
            _bottomNavigationView.setSelectedItemId(_bottomNavigationView.getMenu().getItem(1).getItemId());
        }
        else
        {
            Log.i(TAG, "登录失败:用户名或密码错误");
            Toast.makeText(_context, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 登录完成,用于释放控件
     */
    private void IsLoginEnd()
    {
        _progressBar.setVisibility(View.INVISIBLE);
        _edtUserName.setEnabled(true);
        _edtPassword.setEnabled(true);
        _btnLogin.setEnabled(true);
        _btnForget.setEnabled(true);
        _btnRegister.setEnabled(true);
    }

    /**
     * 正在登录,用于禁止控件
     */
    private void IsLogining()
    {
        _progressBar.setVisibility(View.VISIBLE);
        _edtUserName.setEnabled(false);
        _edtPassword.setEnabled(false);
        _btnLogin.setEnabled(false);
        _btnForget.setEnabled(false);
        _btnRegister.setEnabled(false);
    }

    private void Login()
    {
        _loginTimer = new Timer();
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
                                                _loginTimer.cancel();
                                                Toast.makeText(_context, "登录失败,请检查网络", Toast.LENGTH_SHORT).show();
                                            });
            }
        };
        //请求超时已由Http请求设置,该定时器已弃用
        //任务、延迟执行时间
        //_loginTimer.schedule(loginTask, TIMEINTERVAL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        _bottomNavigationView = getActivity().findViewById(R.id.nav_view);
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
        //_loginTimer.cancel();
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
        if (getArguments() != null)
        {
            _param1 = getArguments().getString(ARG_PARAM1);
            _param2 = getArguments().getString(ARG_PARAM2);
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
        _userView = inflater.inflate(R.layout.fragment_user_login, container, false);
        _context = _userView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/UserInfo/Login";
        _edtUserName = _userView.findViewById(R.id.editTextUserName_login);
        _edtUserName.setOnFocusChangeListener(this);
        _edtPassword = _userView.findViewById(R.id.editTextPassword_login);
        _edtPassword.setOnFocusChangeListener(this);
        _btnLogin = _userView.findViewById(R.id.btn_login_login);
        _btnLogin.setOnClickListener(this);
        _btnRegister = _userView.findViewById(R.id.btn_register_login);
        _btnRegister.setOnClickListener(this);
        _btnForget = _userView.findViewById(R.id.btn_forget_login);
        _btnForget.setOnClickListener(this);
        _progressBar = _userView.findViewById(R.id.progressBar_login);
        return _userView;
    }

    public void onButtonPressed(Uri uri)
    {
        if (_listener != null)
        {
            _listener.onFragmentInteraction(uri);
        }
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

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        _listener = null;
    }

    @Override
    public void onClick(View v)
    {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        switch (v.getId())
        {
            //登录,这里是发起登录请求
            case R.id.btn_login_login:
                if (Pattern.matches(REGEXUSERNAME, _edtUserName.getText().toString()))
                {
                    _edtUserName.setError(null);
                }
                else
                {
                    _edtUserName.setError("用户名非法");
                }
                if (Pattern.matches(REGEXPASSWORD, _edtPassword.getText().toString()))
                {
                    _edtPassword.setError(null);
                }
                else
                {
                    _edtPassword.setError("密码非法");
                }
                if (_edtUserName.getError() == null && _edtPassword.getError() == null)
                {
                    Login();
                }
                break;
            //注册,这里是跳转至注册页面
            case R.id.btn_register_login:
                UserFragment_Register userFragment_register = UserFragment_Register.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_register, "userFragment_register").commitNow();
                break;
            //忘记密码,这里是跳转至找回密码页
            case R.id.btn_forget_login:
                UserFragment_Forget userFragment_forget = UserFragment_Forget.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_forget, "userFragment_forget").commitNow();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch (v.getId())
        {
            case R.id.editTextUserName_login:
                if (hasFocus)
                {
                    _edtUserName.setError(null);
                }
                else
                {
                    if (Pattern.matches(REGEXUSERNAME, _edtUserName.getText().toString()))
                    {
                        _edtUserName.setError(null);
                    }
                    else
                    {
                        _edtUserName.setError("用户名非法");
                    }
                }
                break;
            case R.id.editTextPassword_login:
                if (hasFocus)
                {
                    _edtPassword.setError(null);
                }
                else
                {
                    if (Pattern.matches(REGEXPASSWORD, _edtPassword.getText().toString()))
                    {
                        _edtPassword.setError(null);
                    }
                    else
                    {
                        _edtPassword.setError("密码非法");
                    }
                }
                break;
        }
    }

}
