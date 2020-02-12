package com.zistone.gprs.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.mapapi.model.LatLng;
import com.zistone.gprs.R;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.gprs.pojo.LocationInfo;
import com.zistone.gprs.util.MapUtil;
import com.zistone.gprs.util.PropertiesUtil;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MapFragment_TrackQuery extends Fragment implements View.OnClickListener
{
    private static final String TAG = "MapFragment_TrackQuery";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static final SimpleDateFormat SIMPLEDATEFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat SIMPLEDATEFORMAT_YMD = new SimpleDateFormat("yyyy-MM-dd");
    private static String URL;
    private Context _context;
    private View _trackQueryView;
    private ImageButton _btnReturn;
    private Button _btnQuery;
    private OnFragmentInteractionListener _listener;
    private DeviceInfo _deviceInfo;
    private TextView _txtBegin;
    private TextView _txtEnd;
    //地图工具
    private MapUtil _mapUtil = null;
    //查询轨迹的开始时间
    private String _startStr;
    //查询轨迹的结束时间
    private String _endStr;

    public static MapFragment_TrackQuery newInstance(DeviceInfo deviceInfo)
    {
        MapFragment_TrackQuery fragment = new MapFragment_TrackQuery();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        //隐藏键盘
        InputMethodManager imm =
                (InputMethodManager) _context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        switch(v.getId())
        {
            case R.id.text_beginTime_trackQuery:
            {
                DatePickerDialog.OnDateSetListener onDateSetListener =
                        (view, y, m, d) -> _txtBegin.setText(y + "-" + ++m + "-" + d);
                DatePickerDialog datePickerDialog = new DatePickerDialog(_context, onDateSetListener, year, month, day);
                datePickerDialog.show();
                break;
            }
            case R.id.text_endTime_trackQuery:
            {
                DatePickerDialog.OnDateSetListener onDateSetListener =
                        (view, y, m, d) -> _txtEnd.setText(y + "-" + ++m + "-" + d);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), onDateSetListener, year, month, day);
                datePickerDialog.show();
                break;
            }
            case R.id.btn_return_trackQuery:
                MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").commitNow();
                break;
            case R.id.btn_query_trackQuery:
                QueryHistoryTrack();
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
        if(_listener != null)
        {
            _listener.onFragmentInteraction(uri);
        }
    }

    private Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            super.handleMessage(message);
            switch(message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    if(null == result || "".equals(result) || "[]".equals(result))
                    {
                        //绘制历史轨迹
                        _mapUtil.drawHistoryTrack(null);
                        return;
                    }
                    List<LocationInfo> locationList = JSON.parseArray(result, LocationInfo.class);
                    List<LatLng> trackPointList = new ArrayList<>();
                    if(null != locationList)
                    {
                        for(LocationInfo locationInfo : locationList)
                        {
                            trackPointList.add(new LatLng(locationInfo.getLat(), locationInfo.getLot()));
                        }
                    }
                    //绘制历史轨迹
                    _mapUtil.drawHistoryTrack(trackPointList);
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "获取数据失败,请与管理员联系", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    private void QueryHistoryTrack()
    {
        Date startDate = null;
        Date endDate = null;
        try
        {
            _startStr = _txtBegin.getText() + " 00:00:00";
            startDate = SIMPLEDATEFORMAT.parse(_startStr);
        }
        catch(ParseException e)
        {
            try
            {
                _startStr = "2019-01-01 00:00:00";
                startDate = SIMPLEDATEFORMAT.parse(_startStr);
            }
            catch(ParseException e1)
            {
                e1.printStackTrace();
            }
        }
        try
        {
            _endStr = _txtEnd.getText() + " 23:59:59";
            endDate = SIMPLEDATEFORMAT.parse(_endStr);
        }
        catch(Exception e)
        {
            try
            {
                _endStr = SIMPLEDATEFORMAT_YMD.format(new Date()) + " 23:59:59";
                endDate = SIMPLEDATEFORMAT.parse(_endStr);
            }
            catch(ParseException e1)
            {
                e1.printStackTrace();
            }
        }
        Date finalStartDate = startDate;
        Date finalEndDate = endDate;
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            //RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
            FormBody.Builder builder = new FormBody.Builder();
            builder.add("deviceId", _deviceInfo.getDeviceId());
            builder.add("startTime", finalStartDate.getTime() + "");
            builder.add("endTime", finalEndDate.getTime() + "");
            RequestBody requestBody = builder.build();
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //异步请求
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "查询历史轨迹失败:" + e.toString());
                    Message message = handler.obtainMessage(MESSAGE_RREQUEST_FAIL, "请求失败:" + e.toString());
                    handler.sendMessage(message);
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    String responseStr = response.body().string();
                    if(response.isSuccessful())
                    {
                        Log.i(TAG, "查询历史轨迹成功:" + responseStr);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, responseStr);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "查询历史轨迹失败:" + responseStr);
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
        if(getArguments() != null)
        {
            _deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _trackQueryView = inflater.inflate(R.layout.fragment_map_track_query, container, false);

        _context = getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/LocationInfo/FindByDeviceIdAndBetweenTime";
        _btnReturn = _trackQueryView.findViewById(R.id.btn_return_trackQuery);
        _btnReturn.setOnClickListener(this::onClick);
        _btnQuery = _trackQueryView.findViewById(R.id.btn_query_trackQuery);
        _btnQuery.setOnClickListener(this::onClick);
        _txtBegin = _trackQueryView.findViewById(R.id.text_beginTime_trackQuery);
        _txtBegin.setOnClickListener(this::onClick);
        _txtEnd = _trackQueryView.findViewById(R.id.text_endTime_trackQuery);
        _txtEnd.setOnClickListener(this::onClick);
        _mapUtil = MapUtil.getInstance();
        _mapUtil.init(_trackQueryView.findViewById(R.id.mapView_trackQuery));
        //设置地图中心
        LatLng latLng = new LatLng(_deviceInfo.getLat(), _deviceInfo.getLot());
        _mapUtil.UpdateStatus(latLng, false, _context);
        return _trackQueryView;
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
}
