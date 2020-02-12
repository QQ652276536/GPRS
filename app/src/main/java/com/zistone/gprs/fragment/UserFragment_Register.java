package com.zistone.gprs.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;
import com.zistone.gprs.pojo.UserInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
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

public class UserFragment_Register extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String TAG = "UserFragment_Register";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    //6~12位字母数字组合或6位中文
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})|[\\u4e00-\\u9fa5]{2,6}";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    //手机号
    private static final String REGEXPHONE = "^(13|14|15|18|17)[0-9]{9}";
    private String mParam1;
    private String mParam2;
    private Context _context;
    private View _registerView;
    private EditText _edtUserName;
    private EditText _edtRealName;
    private EditText _edtPhone;
    private EditText _edtPwd;
    private EditText _edtRePwd;
    private ImageButton _btnReturn;
    private Button _btnRegister;
    private ProgressBar _progressBar;

    private OnFragmentInteractionListener _listener;

    public static UserFragment_Register newInstance(String param1, String param2)
    {
        UserFragment_Register fragment = new UserFragment_Register();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public void onButtonPressed(Uri uri)
    {
        if(_listener != null)
        {
            _listener.onFragmentInteraction(uri);
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
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "注册超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(result, UserInfo.class);
                    RegisterResult(userInfo);
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "注册失败,请与管理员联系", Toast.LENGTH_SHORT).show();
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
            userInfo.setRealName(_edtRealName.getText().toString());
            userInfo.setUserName(_edtUserName.getText().toString());
            userInfo.setPhoneNumber(_edtPhone.getText().toString());
            userInfo.setPassword(_edtRePwd.getText().toString());
            userInfo.setState(1);
            userInfo.setLevel(1);
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
                    Log.e(TAG, "注册失败:" + e.toString());
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
                    if(response.isSuccessful())
                    {
                        Log.i(TAG, "注册成功:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "注册失败:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, result);
                        handler.sendMessage(message);
                    }
                }
            });
            Looper.loop();
        }).start();
    }

    /**
     * 注册成功与否
     */
    private void RegisterResult(UserInfo userInfo)
    {
        if(userInfo != null)
        {
            Log.i(TAG, "注册成功:用户ID是" + userInfo.getId());
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton("确定", (dialog, which) ->
            {
                dialog.dismiss();
                UserFragment_Login userFragment_login = UserFragment_Login.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_login, "userFragment_login").commitNow();
            });
            builder.setMessage("注册成功,请牢记你的用户名" + userInfo.getUserName() + "和密码!");
            builder.show();
        }
        else
        {
            Log.i(TAG, "注册失败:用户名已被注册");
            Toast.makeText(_context, "注册失败:用户名已被注册", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 注册完成,用于释放控件
     */
    private void IsRegisterEd()
    {
        _progressBar.setVisibility(View.INVISIBLE);
        _edtUserName.setEnabled(true);
        _edtRealName.setEnabled(true);
        _edtPhone.setEnabled(true);
        _edtPwd.setEnabled(true);
        _edtRePwd.setEnabled(true);
        _btnReturn.setEnabled(true);
        _btnRegister.setEnabled(true);
    }

    /**
     * 正在注册,用于禁止控件
     */
    private void IsRegistering()
    {
        _progressBar.setVisibility(View.VISIBLE);
        _edtUserName.setEnabled(false);
        _edtRealName.setEnabled(false);
        _edtPhone.setEnabled(false);
        _edtPwd.setEnabled(false);
        _edtRePwd.setEnabled(false);
        //_btnReturn.setEnabled(false);
        _btnRegister.setEnabled(false);
    }

    private void Register()
    {
        IsRegistering();
        SendWithOkHttp();
    }

    @Override
    public void onClick(View v)
    {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        switch(v.getId())
        {
            case R.id.btn_return_register:
                UserFragment_Login userFragment_login = UserFragment_Login.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_login, "userFragment_login").commitNow();
                break;
            case R.id.btn_register_register:
                if(Pattern.matches(REGEXUSERNAME, _edtUserName.getText().toString()))
                {
                    _edtUserName.setError(null);
                }
                else
                {
                    _edtUserName.setError("用户名非法");
                }
                if(Pattern.matches(REGEXUSERNAME, _edtRealName.getText().toString()))
                {
                    _edtRealName.setError(null);
                }
                else
                {
                    _edtRealName.setError("姓名非法");
                }
                if(Pattern.matches(REGEXPHONE, _edtPhone.getText().toString()))
                {
                    _edtPhone.setError(null);
                }
                else
                {
                    _edtPhone.setError("手机号非法");
                }
                if(Pattern.matches(REGEXPASSWORD, _edtPwd.getText().toString()))
                {
                    _edtPwd.setError(null);
                }
                else
                {
                    _edtPwd.setError("密码非法");
                }
                if(Pattern.matches(REGEXPASSWORD, _edtRePwd.getText().toString()))
                {
                    if(!_edtRePwd.getText().toString().equals(_edtPwd.getText().toString()))
                    {
                        _edtRePwd.setError("两次密码不一致");
                    }
                    else
                    {
                        _edtRePwd.setError(null);
                    }
                }
                else
                {
                    _edtRePwd.setError("密码非法");
                }
                if(_edtUserName.getError() == null && _edtRealName.getError() == null && _edtPhone.getError() == null && _edtPwd.getError() == null && _edtRePwd.getError() == null)
                {
                    Register();
                }
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch(v.getId())
        {
            case R.id.editTextUserName_register:
                if(hasFocus)
                {
                    _edtUserName.setError(null);
                }
                else
                {
                    if(Pattern.matches(REGEXUSERNAME, _edtUserName.getText().toString()))
                    {
                        _edtUserName.setError(null);
                    }
                    else
                    {
                        _edtUserName.setError("用户名非法");
                    }
                }
                break;
            case R.id.editTextRealName_register:
                if(hasFocus)
                {
                    _edtRealName.setError(null);
                }
                else
                {
                    if(Pattern.matches(REGEXUSERNAME, _edtRealName.getText().toString()))
                    {
                        _edtRealName.setError(null);
                    }
                    else
                    {
                        _edtRealName.setError("姓名非法");
                    }
                }
                break;
            case R.id.editTextPhone_register:
                if(hasFocus)
                {
                    _edtPhone.setError(null);
                }
                else
                {
                    if(Pattern.matches(REGEXPHONE, _edtPhone.getText().toString()))
                    {
                        _edtPhone.setError(null);
                    }
                    else
                    {
                        _edtPhone.setError("手机号非法");
                    }
                }
                break;
            case R.id.editTextPassword_register:
                if(hasFocus)
                {
                    _edtPwd.setError(null);
                }
                else
                {
                    if(Pattern.matches(REGEXPASSWORD, _edtPwd.getText().toString()))
                    {
                        _edtPwd.setError(null);
                    }
                    else
                    {
                        _edtPwd.setError("密码非法");
                    }
                }
                break;
            case R.id.editTextRePassword_register:
                if(hasFocus)
                {
                    _edtRePwd.setError(null);
                }
                else
                {
                    if(Pattern.matches(REGEXPASSWORD, _edtRePwd.getText().toString()))
                    {
                        if(!_edtRePwd.getText().toString().equals(_edtPwd.getText().toString()))
                        {
                            _edtRePwd.setError("再次密码不一致");
                        }
                        else
                        {
                            _edtRePwd.setError(null);
                        }
                    }
                    else
                    {
                        _edtRePwd.setError("密码非法");
                    }
                }
                break;
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
        _registerView = inflater.inflate(R.layout.fragment_user_register, container, false);
        _context = _registerView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/UserInfo/Register";
        _edtUserName = _registerView.findViewById(R.id.editTextUserName_register);
        _edtUserName.setOnFocusChangeListener(this);
        _edtRealName = _registerView.findViewById(R.id.editTextRealName_register);
        _edtRealName.setOnFocusChangeListener(this);
        _edtPhone = _registerView.findViewById(R.id.editTextPhone_register);
        _edtPhone.setOnFocusChangeListener(this);
        _edtPwd = _registerView.findViewById(R.id.editTextPassword_register);
        _edtPwd.setOnFocusChangeListener(this);
        _edtRePwd = _registerView.findViewById(R.id.editTextRePassword_register);
        _edtRePwd.setOnFocusChangeListener(this);
        _btnReturn = _registerView.findViewById(R.id.btn_return_register);
        _btnReturn.setOnClickListener(this);
        _btnRegister = _registerView.findViewById(R.id.btn_register_register);
        _btnRegister.setOnClickListener(this);
        _progressBar = _registerView.findViewById(R.id.progressBar_register);
        return _registerView;
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

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener)
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

}
