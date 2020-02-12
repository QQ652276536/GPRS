package com.zistone.gprs.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;

public class UserFragment_Forget extends Fragment implements View.OnClickListener
{
    private static final String TAG = "UserFragment_Forget";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static String URL;
    private String mParam1;
    private String mParam2;
    private Context _context;
    private View _forgetView;
    private Toolbar _toolbar;
    private ImageButton _btnReturn;

    private OnFragmentInteractionListener _listener;

    public static UserFragment_Forget newInstance(String param1, String param2)
    {
        UserFragment_Forget fragment = new UserFragment_Forget();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        _forgetView = inflater.inflate(R.layout.fragment_user_forget, container, false);

        _context = getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/UserInfo/Forget";
        _toolbar = _forgetView.findViewById(R.id.toolbar_forget);
        _btnReturn = _forgetView.findViewById(R.id.btn_return_forget);
        _btnReturn.setOnClickListener(this);
        return _forgetView;
    }

    public void onButtonPressed(Uri uri)
    {
        if(_listener != null)
        {
            _listener.onFragmentInteraction(uri);
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

    @Override
    public void onDetach()
    {
        super.onDetach();
        _listener = null;
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
