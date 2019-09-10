package com.zistone.blowdown_app.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zistone.blowdown_app.PropertiesUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.entity.UserInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TrackQueryFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String TAG = "TrackQueryFragment";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context m_context;
    private View m_trackQueryView;
    private ImageButton m_btnReturn;
    private OnFragmentInteractionListener mListener;
    private Button m_btnQuery;
    private DeviceInfo m_deviceInfo;
    private EditText m_editBegin;
    private EditText m_editend;

    public static TrackQueryFragment newInstance(DeviceInfo deviceInfo)
    {
        TrackQueryFragment fragment = new TrackQueryFragment();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        switch(v.getId())
        {
            case R.id.btn_return_trackQuery:
                MapFragment mapFragment = MapFragment.newInstance(m_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current, mapFragment, "mapFragment").commitNow();
                break;
            case R.id.btn_query_trackQuery:
                break;
            case R.id.editText_beginTime_trackQuery:

                break;
            case R.id.editText_endTime_trackQuery:
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        switch(v.getId())
        {
            case R.id.editText_beginTime_trackQuery:
                if(hasFocus)
                {
                    DatePickerDialog.OnDateSetListener onDateSetListener = (view, y, m, d) -> m_editBegin.setText(y + "-" + m + 1 + "-" + d);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
                    datePickerDialog.show();
                }
                break;
            case R.id.editText_endTime_trackQuery:
                if(hasFocus)
                {
                    DatePickerDialog.OnDateSetListener onDateSetListener = (view, y, m, d) -> m_editend.setText(y + "-" + m + 1 + "-" + d);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
                    datePickerDialog.show();
                }
                break;
        }
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = getContext();
        m_btnReturn = m_trackQueryView.findViewById(R.id.btn_return_trackQuery);
        m_btnReturn.setOnClickListener(this);
        m_editBegin = m_trackQueryView.findViewById(R.id.editText_beginTime_trackQuery);
        m_editBegin.setOnFocusChangeListener(this);
        m_editBegin.setInputType(InputType.TYPE_NULL);
        m_editend = m_trackQueryView.findViewById(R.id.editText_endTime_trackQuery);
        m_editend.setOnFocusChangeListener(this);
        m_editend.setInputType(InputType.TYPE_NULL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            m_deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_trackQueryView = inflater.inflate(R.layout.fragment_track_query, container, false);
        InitView();
        return m_trackQueryView;
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
}
