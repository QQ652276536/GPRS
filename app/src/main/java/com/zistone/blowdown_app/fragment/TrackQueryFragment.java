package com.zistone.blowdown_app.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.analysis.DrivingBehaviorRequest;
import com.baidu.trace.api.analysis.DrivingBehaviorResponse;
import com.baidu.trace.api.analysis.HarshAccelerationPoint;
import com.baidu.trace.api.analysis.HarshBreakingPoint;
import com.baidu.trace.api.analysis.HarshSteeringPoint;
import com.baidu.trace.api.analysis.OnAnalysisListener;
import com.baidu.trace.api.analysis.SpeedingInfo;
import com.baidu.trace.api.analysis.SpeedingPoint;
import com.baidu.trace.api.analysis.StayPoint;
import com.baidu.trace.api.analysis.StayPointRequest;
import com.baidu.trace.api.analysis.StayPointResponse;
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
import com.zistone.blowdown_app.TrackApplication;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.util.BitmapUtil;
import com.zistone.blowdown_app.util.CommonUtil;
import com.zistone.blowdown_app.util.Constants;
import com.zistone.blowdown_app.util.MapUtil;
import com.zistone.blowdown_app.util.ViewUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
    //历史轨迹请求
    private HistoryTrackRequest m_historyTrackRequest = new HistoryTrackRequest();
    //轨迹监听器（用于接收历史轨迹回调）
    private OnTrackListener m_onTrackListener = null;
    //地图工具
    private MapUtil m_mapUtil = null;
    private TrackApplication m_trackApplication;
    private ViewUtil m_viewUtil = null;
    //轨迹分析监听器
    private OnAnalysisListener m_onAnalysisListener = null;
    //当前轨迹分析详情框对应的marker
    private Marker m_analysisMarker = null;
    //驾驶行为请求
    private DrivingBehaviorRequest m_drivingBehaviorRequest = new DrivingBehaviorRequest();
    //停留点请求
    private StayPointRequest m_stayPointRequest = new StayPointRequest();
    //轨迹分析监听器
    private OnAnalysisListener m_mAnalysisListener = null;
    //查询轨迹的开始时间
    private long m_startTime = CommonUtil.getCurrentTime();
    //查询轨迹的结束时间
    private long m_endTime = CommonUtil.getCurrentTime();
    //轨迹点集合
    private List<LatLng> m_trackPointList = new ArrayList<>();
    //轨迹分析  超速点集合
    private List<Point> m_speedingPointList = new ArrayList<>();
    //轨迹分析  急加速点集合
    private List<Point> m_harshAccelPointList = new ArrayList<>();
    //轨迹分析  急刹车点集合
    private List<Point> m_harshBreakingPointList = new ArrayList<>();
    //轨迹分析  急转弯点集合
    private List<Point> m_harshSteeringPointList = new ArrayList<>();
    //轨迹分析  停留点集合
    private List<Point> m_stayPointList = new ArrayList<>();
    //轨迹分析 超速点覆盖物集合
    private List<Marker> m_speedingMarkerList = new ArrayList<>();
    //轨迹分析 急加速点覆盖物集合
    private List<Marker> m_harshAccelMarkerList = new ArrayList<>();
    //轨迹分析  急刹车点覆盖物集合
    private List<Marker> m_harshBreakingMarkerList = new ArrayList<>();
    //轨迹分析  急转弯点覆盖物集合
    private List<Marker> m_harshSteeringMarkerList = new ArrayList<>();
    //轨迹分析  停留点覆盖物集合
    private List<Marker> m_stayPointMarkerList = new ArrayList<>();
    //是否查询超速点
    private boolean m_isSpeeding = false;
    //是否查询急加速点
    private boolean m_isHarshAccel = false;
    //是否查询急刹车点
    private boolean m_isHarshBreaking = false;
    //是否查询急转弯点
    private boolean m_isHarshSteering = false;
    //是否查询停留点
    private boolean m_isStayPoint = false;
    private int m_pageIndex = 1;
    //轨迹分析上一次请求时间
    private long m_lastQueryTime = 0;

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
        m_trackApplication = (TrackApplication) m_context;
        //设置地图中心
        m_mapUtil.updateStatus(new LatLng(m_deviceInfo.getM_lat(), m_deviceInfo.getM_lot()), false);
        InitListener();
    }

    private void InitListener()
    {
        m_onTrackListener = new OnTrackListener()
        {
            @Override
            public void onHistoryTrackCallback(HistoryTrackResponse response)
            {
                int total = response.getTotal();
                if(StatusCodes.SUCCESS != response.getStatus())
                {
                    m_viewUtil.showToast(getTargetFragment(), response.getMessage());
                }
                else if(0 == total)
                {
                    m_viewUtil.showToast(getTargetFragment(), "未查询到轨迹");
                }
                else
                {
                    List<TrackPoint> points = response.getTrackPoints();
                    if(null != points)
                    {
                        for(TrackPoint trackPoint : points)
                        {
                            if(!CommonUtil.isZeroPoint(trackPoint.getLocation().getLatitude(), trackPoint.getLocation().getLongitude()))
                            {
                                m_trackPointList.add(MapUtil.convertTrace2Map(trackPoint.getLocation()));
                            }
                        }
                    }
                }

                if(total > Constants.PAGE_SIZE * m_pageIndex)
                {
                    m_historyTrackRequest.setPageIndex(++m_pageIndex);
                    //查询历史轨迹
                    QueryHistoryTrack();
                }
                else
                {
                    m_mapUtil.drawHistoryTrack(m_trackPointList, SortType.asc);
                }
            }

            @Override
            public void onDistanceCallback(DistanceResponse response)
            {
                super.onDistanceCallback(response);
            }

            @Override
            public void onLatestPointCallback(LatestPointResponse response)
            {
                super.onLatestPointCallback(response);
            }
        };

        m_onAnalysisListener = new OnAnalysisListener()
        {
            @Override
            public void onStayPointCallback(StayPointResponse response)
            {
                if(StatusCodes.SUCCESS != response.getStatus())
                {
                    m_lastQueryTime = 0;
                    m_viewUtil.showToast(getTargetFragment(), response.getMessage());
                    return;
                }
                if(0 == response.getStayPointNum())
                {
                    return;
                }
                m_stayPointList.addAll(response.getStayPoints());
                handleOverlays(m_stayPointMarkerList, m_stayPointList, m_isStayPoint);
            }

            @Override
            public void onDrivingBehaviorCallback(DrivingBehaviorResponse response)
            {
                if(StatusCodes.SUCCESS != response.getStatus())
                {
                    m_lastQueryTime = 0;
                    m_viewUtil.showToast(getTargetFragment(), response.getMessage());
                    return;
                }

                if(0 == response.getSpeedingNum() && 0 == response.getHarshAccelerationNum() && 0 == response.getHarshBreakingNum() && 0 == response.getHarshSteeringNum())
                {
                    return;
                }

                clearAnalysisList();
                clearAnalysisOverlay();

                List<SpeedingInfo> speedingInfos = response.getSpeedings();
                for(SpeedingInfo info : speedingInfos)
                {
                    m_speedingPointList.addAll(info.getPoints());
                }
                m_harshAccelPointList.addAll(response.getHarshAccelerationPoints());
                m_harshBreakingPointList.addAll(response.getHarshBreakingPoints());
                m_harshSteeringPointList.addAll(response.getHarshSteeringPoints());

                handleOverlays(m_speedingMarkerList, m_speedingPointList, m_isSpeeding);
                handleOverlays(m_harshAccelMarkerList, m_harshAccelPointList, m_isHarshAccel);
                handleOverlays(m_harshBreakingMarkerList, m_harshBreakingPointList, m_isHarshBreaking);
                handleOverlays(m_harshSteeringMarkerList, m_harshSteeringPointList, m_isHarshSteering);
            }
        };
    }

    /**
     * 清除驾驶行为分析覆盖物
     */
    public void clearAnalysisOverlay()
    {
        clearOverlays(m_speedingMarkerList);
        clearOverlays(m_harshAccelMarkerList);
        clearOverlays(m_harshBreakingMarkerList);
        clearOverlays(m_stayPointMarkerList);
    }

    private void clearOverlays(List<Marker> markers)
    {
        if(null == markers)
        {
            return;
        }
        for(Marker marker : markers)
        {
            marker.remove();
        }
        markers.clear();
    }

    private void clearAnalysisList()
    {
        if(null != m_speedingPointList)
        {
            m_speedingPointList.clear();
        }
        if(null != m_harshAccelPointList)
        {
            m_harshAccelPointList.clear();
        }
        if(null != m_harshBreakingPointList)
        {
            m_harshBreakingPointList.clear();
        }
        if(null != m_harshSteeringPointList)
        {
            m_harshSteeringPointList.clear();
        }
    }

    /**
     * 处理轨迹分析覆盖物
     *
     * @param markers
     * @param points
     * @param isVisible
     */
    private void handleOverlays(List<Marker> markers, List<? extends com.baidu.trace.model.Point> points, boolean isVisible)
    {
        if(null == markers || null == points)
        {
            return;
        }
        for(com.baidu.trace.model.Point point : points)
        {
            OverlayOptions overlayOptions = new MarkerOptions().position(MapUtil.convertTrace2Map(point.getLocation())).icon(BitmapUtil.bmGcoding).zIndex(9).draggable(true);
            Marker marker = (Marker) m_mapUtil.baiduMap.addOverlay(overlayOptions);
            Bundle bundle = new Bundle();
            marker.setExtraInfo(bundle);
            markers.add(marker);
        }
        handleMarker(markers, isVisible);
    }

    /**
     * 处理marker
     *
     * @param markers
     * @param isVisible
     */
    private void handleMarker(List<Marker> markers, boolean isVisible)
    {
        if(null == markers || markers.isEmpty())
        {
            return;
        }
        for(Marker marker : markers)
        {
            marker.setVisible(isVisible);
        }

        if(markers.contains(m_analysisMarker))
        {
            m_mapUtil.baiduMap.hideInfoWindow();
        }
    }

    private void QueryHistoryTrack()
    {
        m_trackApplication.initRequest(m_historyTrackRequest);
        m_historyTrackRequest.setEntityName(m_trackApplication.entityName);
        m_historyTrackRequest.setStartTime(m_startTime);
        m_historyTrackRequest.setEndTime(m_endTime);
        m_historyTrackRequest.setPageIndex(m_pageIndex);
        m_historyTrackRequest.setPageSize(Constants.PAGE_SIZE);
        m_trackApplication.mClient.queryHistoryTrack(m_historyTrackRequest, m_onTrackListener);
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
