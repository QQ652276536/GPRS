package com.zistone.blowdown_app.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.analysis.DrivingBehaviorRequest;
import com.baidu.trace.api.analysis.OnAnalysisListener;
import com.baidu.trace.api.analysis.StayPointRequest;
import com.baidu.trace.api.track.DistanceResponse;
import com.baidu.trace.api.track.HistoryTrackRequest;
import com.baidu.trace.api.track.HistoryTrackResponse;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.api.track.TrackPoint;
import com.baidu.trace.model.Point;
import com.baidu.trace.model.SortType;
import com.baidu.trace.model.StatusCodes;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.DeviceInfoRecyclerAdapter;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.entity.LocationInfo;
import com.zistone.blowdown_app.util.CommonUtil;
import com.zistone.blowdown_app.util.Constants;
import com.zistone.blowdown_app.util.MapUtil;
import com.zistone.blowdown_app.util.PropertiesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

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
    //地图工具
    private MapUtil m_mapUtil = null;
    //查询轨迹的开始时间
    private String m_startStr;
    //查询轨迹的结束时间
    private String m_endStr;

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
        switch (v.getId())
        {
            case R.id.btn_return_trackQuery:
                MapFragment mapFragment = MapFragment.newInstance(m_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current, mapFragment, "mapFragment").commitNow();
                break;
            case R.id.btn_query_trackQuery:
                QueryHistoryTrack();
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
        switch (v.getId())
        {
            case R.id.editText_beginTime_trackQuery:
                if (hasFocus)
                {
                    DatePickerDialog.OnDateSetListener onDateSetListener = (view, y, m, d) -> m_editBegin.setText(y + "-" + m + 1 + "-" + d);
                    DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
                    datePickerDialog.show();
                }
                break;
            case R.id.editText_endTime_trackQuery:
                if (hasFocus)
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
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/LocationInfo/FindByDeviceId";
        m_btnReturn = m_trackQueryView.findViewById(R.id.btn_return_trackQuery);
        m_btnReturn.setOnClickListener(this::onClick);
        m_editBegin = m_trackQueryView.findViewById(R.id.editText_beginTime_trackQuery);
        m_editBegin.setOnFocusChangeListener(this::onFocusChange);
        m_editBegin.setInputType(InputType.TYPE_NULL);
        m_editend = m_trackQueryView.findViewById(R.id.editText_endTime_trackQuery);
        m_editend.setOnFocusChangeListener(this::onFocusChange);
        m_editend.setInputType(InputType.TYPE_NULL);
        m_btnQuery = m_trackQueryView.findViewById(R.id.btn_query_trackQuery);
        m_btnQuery.setOnClickListener(this::onClick);
        m_mapUtil = MapUtil.getInstance();
        m_mapUtil.init(m_trackQueryView.findViewById(R.id.mapView_trackQuery));
        //设置地图中心
        LatLng latLng = new LatLng(m_deviceInfo.getM_lat(), m_deviceInfo.getM_lot());
        m_mapUtil.UpdateStatus(latLng, false, m_context);
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch (message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(m_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    if (null == result || "".equals(result))
                    {
                        return;
                    }
                    List<LocationInfo> locationList = JSON.parseArray(result, LocationInfo.class);
                    List<LatLng> trackPointList = new ArrayList<>();
                    if (null != locationList)
                    {
                        for (LocationInfo locationInfo : locationList)
                        {
                            trackPointList.add(MapUtil.convertTrace2Map(new com.baidu.trace.model.LatLng(locationInfo.getM_lat(), locationInfo.getM_lot())));
                        }
                    }
                    //绘制历史轨迹
                    m_mapUtil.drawHistoryTrack(trackPointList, SortType.asc);
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(m_context, "获取数据失败,请与管理员联系", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void QueryHistoryTrack()
    {
        m_startStr = m_editBegin.getText().toString();
        m_endStr = m_editend.getText().toString();
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
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
                    Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    String responseStr = response.body().string();
                    Log.i(TAG, "响应内容:" + responseStr);
                    if (response.isSuccessful())
                    {
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, responseStr);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, responseStr);
                        handler.sendMessage(message);
                    }
                }
            });
            Looper.loop();
        }).start();
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
        if (getArguments() != null)
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
        if (context instanceof OnFragmentInteractionListener)
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
