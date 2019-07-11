package com.example.blowdown_app;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private View m_userView;
    private EditText m_editText_userName;
    private EditText m_editText_password;
    private Button m_btn_login;
    private Button m_btn_register;
    private Button m_btn_forget;

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
     *该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_login == v.getId()) {
        } else if (R.id.btn_register.getId() == v.getId()) {
        } else if (R.id.btn_forget.getId() == v.getId()) {
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    private void InitData() {
        m_editText_userName = m_userView.findViewById(R.id.editTextUserName);
        m_editText_password = m_userView.findViewById(R.id.editTextPasswrod);
        m_btn_login = m_userView.findViewById(R.id.btn_login);
        m_btn_login.setOnClickListener(this);
        m_btn_register = m_userView.findViewById(R.id.btn_register);
        m_btn_register.setOnClickListener(this);
        m_btn_forget = m_userView.findViewById(R.id.btn_forget);
        m_btn_forget.setOnClickListener(this);
    }
}
