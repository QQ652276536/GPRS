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
import android.widget.TextView;

import com.example.blowdown_app.http.HttpClientUtil;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL = "http://10.0.2.2:8080/Blowdown/UserInfo/Login";
    private static final int SHOW_RESPONSE = 0;

    private String mParam1;
    private String mParam2;
    private boolean m_userNameFlag = false;
    private boolean m_passwordFlag = false;

    private View m_userView;
    private EditText m_editText_userName;
    private EditText m_editText_password;
    private Button m_btn_login;
    private Button m_btn_register;
    private Button m_btn_forget;
    private ProgressBar m_loginProgressBar;
    private TextView textView;

    private OnFragmentInteractionListener mListener;

    public UserFragment() {
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
    public static UserFragment newInstance(String param1, String param2) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        m_userView = inflater.inflate(R.layout.fragment_user, container, false);
        InitData();
        return m_userView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_login == v.getId()) {

            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }

            if (!m_userNameFlag) {
                m_editText_userName.setError("用户名非法");
            }
            if (!m_passwordFlag) {
                m_editText_password.setError("密码非法");
            }
            if (m_userNameFlag && m_passwordFlag) {
                Login();
            }
        } else if (R.id.btn_register == v.getId()) {
        } else if (R.id.btn_forget == v.getId()) {
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (R.id.editTextUserName == v.getId()) {
            if (hasFocus) {
                m_editText_userName.setError(null);
            } else {
                String regex = "\\w{3,12}";
                if (Pattern.matches(regex, m_editText_userName.getText().toString())) {
                    m_editText_userName.setError(null);
                    m_userNameFlag = true;
                } else {
                    m_userNameFlag = false;
                    m_editText_userName.setError("用户名非法");
                }
            }
        }
        if (R.id.editTextPasswrod == v.getId()) {
            if (hasFocus) {
                m_editText_password.setError(null);
            } else {
                //首位不能是数字,不能全为数字或字母,6~16位
                String regex = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
                if (Pattern.matches(regex, m_editText_password.getText().toString())) {
                    m_editText_password.setError(null);
                    m_passwordFlag = true;
                } else {
                    m_passwordFlag = false;
                    m_editText_password.setError("密码非法");
                }
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void IsLogining() {
        m_loginProgressBar.setVisibility(View.VISIBLE);
        m_editText_userName.setEnabled(false);
        m_editText_password.setEnabled(false);
        m_btn_login.setEnabled(false);
        m_btn_forget.setEnabled(false);
        m_btn_register.setEnabled(false);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
                case SHOW_RESPONSE:
                    String responseStr = (String) message.obj;
                    textView.setText(responseStr);
                    break;
            }
        }
    };

    /**
     * 发送网络请求,在里面开启线程
     */
    private void SendWithHttpClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
        //TODO:跳转至
    }

    private void Login() {
        IsLogining();
        SendWithHttpClient();
    }

    private void InitData() {
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
        textView = m_userView.findViewById(R.id.textView);
    }
}
