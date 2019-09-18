package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zistone.blowdown_app.util.PropertiesUtil;
import com.zistone.blowdown_app.R;

public class UserFragment_Forget extends Fragment implements View.OnClickListener
{
    private static final String TAG = "UserFragment_Forget";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String URL;
    private String mParam1;
    private String mParam2;
    private Context m_context;
    private View m_forgetView;
    private Toolbar m_toolbar;
    private ImageButton m_btnReturn;

    private OnFragmentInteractionListener mListener;

    public static UserFragment_Forget newInstance(String param1, String param2)
    {
        UserFragment_Forget fragment = new UserFragment_Forget();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private void InitView()
    {
        m_context = getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/UserInfo/Forget";
        m_toolbar = m_forgetView.findViewById(R.id.toolbar_forget);
        m_btnReturn = m_forgetView.findViewById(R.id.btn_return_forget);
        m_btnReturn.setOnClickListener(this);
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_forgetView = inflater.inflate(R.layout.fragment_user_forget, container, false);
        InitView();
        return m_forgetView;
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

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return_forget:
                UserFragment_Login userFragment_login = UserFragment_Login.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_login, "userFragment_login").commitNow();
                break;
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }
}
