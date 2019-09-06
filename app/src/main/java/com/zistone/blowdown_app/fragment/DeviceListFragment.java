package com.zistone.blowdown_app.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blowdown_app.PropertiesUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.DeviceInfoRecyclerAdapter;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceListFragment extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceListFragment";
    private static final String ARG_PARAM1 = "DEVICESTATE";
    private static final String ARG_PARAM2 = "param2";
    private static final int TIMEINTERVAL = 30 * 1000;
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    private static String URL;
    private Context m_context;
    private View m_deviceListView;
    private List<DeviceInfo> m_deviceList = new ArrayList<>();
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    //RecyclerView
    private RecyclerView m_recyclerView;
    //适配器
    private DeviceInfoRecyclerAdapter m_deviceInfoRecyclerAdapter;
    private Timer m_refreshTimer;
    //设备状态
    private int m_deviceState;
    private TextView m_ToolbarTextView;
    private ImageButton m_btnReturn;
    //底部导航栏
    public BottomNavigationView m_bottomNavigationView;
    private OnFragmentInteractionListener mListener;

    /**
     * @param param1 设备状态
     * @param param2
     * @return
     */
    public static DeviceListFragment newInstance(int param1, String param2)
    {
        DeviceListFragment fragment = new DeviceListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        if(R.id.btn_return_device_list == v.getId())
        {
            DeviceManageFragment deviceManageFragment = DeviceManageFragment.newInstance("", "");
            getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceManageFragment, "deviceManageFragment").commitNow();
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

    /**
     * 定时刷新设备列表
     */
    private void RefreshDeviceList()
    {
        m_refreshTimer = new Timer();
        TimerTask refreshTask = new TimerTask()
        {
            @Override
            public void run()
            {
                //返回到UI线程,两种更新UI的方法之一
                getActivity().runOnUiThread(() -> SendWithOkHttp());
            }
        };
        //任务、延迟执行时间、重复调用间隔
        m_refreshTimer.schedule(refreshTask, 0, TIMEINTERVAL);
    }

    public void InitView()
    {
        m_context = m_deviceListView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/DeviceInfo/FindAll";
        m_btnReturn = m_deviceListView.findViewById(R.id.btn_return_device_list);
        m_btnReturn.setOnClickListener(this::onClick);
        m_ToolbarTextView = m_deviceListView.findViewById(R.id.textView_toolbar);
        m_recyclerView = m_deviceListView.findViewById(R.id.device_recycler);
        m_deviceState = getArguments().getInt("DEVICESTATE");
        if(m_deviceState == 1)
        {
            m_ToolbarTextView.setText("可用设备列表");
        }
        else
        {
            m_ToolbarTextView.setText("停用设备列表");
        }
        //下拉刷新控件
        m_materialRefreshLayout = m_deviceListView.findViewById(R.id.refresh);
        //禁用加载更多
        m_materialRefreshLayout.setLoadMore(false);
        m_materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener()
        {
            /**
             * 下拉刷新
             * @param materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout materialRefreshLayout)
            {
                materialRefreshLayout.postDelayed(() ->
                {
                    SendWithOkHttp();
                    //结束下拉刷新
                    materialRefreshLayout.finishRefresh();
                }, 1 * 1000);
            }

            /**
             * 加载完毕
             */
            @Override
            public void onfinish()
            {
                //Toast.makeText(m_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout)
            {
                Toast.makeText(m_context, "别滑了,到底了", Toast.LENGTH_LONG).show();
            }
        });
        //自动刷新
        m_materialRefreshLayout.autoRefresh();
        //使用线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(m_context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //设置布局
        m_recyclerView.setLayoutManager(linearLayoutManager);
        //TODO:设置适配器是在异步回调里设的,所以启动时会有No adapter attached; skipping layout异常
        RefreshDeviceList();
    }

    /**
     * 给下拉控件设置点击事件
     */
    private void SetDeviceInfoRecyclerAdapterListener()
    {
        if(null != m_deviceInfoRecyclerAdapter)
        {
            m_deviceInfoRecyclerAdapter.SetOnItemClickListener(new DeviceInfoRecyclerAdapter.OnItemClickListener()
            {
                @Override
                public void OnClick(int position)
                {
                    DeviceInfo tempDevice = m_deviceList.get(position);
                    Toast.makeText(m_context, tempDevice.getM_name(), Toast.LENGTH_SHORT).show();
                    int fragmentSize = getParentFragment().getFragmentManager().getFragments().size();
                    //使用父级的去获取Fragment管理器
                    Fragment deviceFragment = getParentFragment().getFragmentManager().findFragmentByTag("deviceFragment");
                    //隐藏当前设备页
                    getParentFragment().getFragmentManager().beginTransaction().hide(deviceFragment).commitNow();
                    //地图页已经实例化过则从管理器中移除之前的地图页
                    Fragment oldMapFragment = getParentFragment().getFragmentManager().findFragmentByTag("mapFragment");
                    if(oldMapFragment != null)
                    {
                        getParentFragment().getFragmentManager().beginTransaction().remove(oldMapFragment).commitNow();
                    }
                    //重新实例化地图碎片实现重新加载设备位置
                    MapFragment newMapFragment = MapFragment.newInstance(tempDevice);
                    getParentFragment().getFragmentManager().beginTransaction().add(R.id.fragment_current, newMapFragment, "mapFragment").show(newMapFragment).commitNow();
                    //通过点击设备列表切换到地图时修改bottomNavigation控件的选中效果
                    m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(0).getItemId());
                }

                @Override
                public void OnLongClick(int position)
                {
                    Toast.makeText(m_context, "当前长按 " + position, Toast.LENGTH_SHORT).show();
                }
            });
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
                case MESSAGE_GETRESPONSE_SUCCESS:
                {
                    String responseStr = (String) message.obj;
                    if(null == responseStr || "".equals(responseStr))
                    {
                        return;
                    }
                    m_deviceList = JSON.parseArray(responseStr, DeviceInfo.class);
                    //在线设备
                    if(m_deviceState == 1)
                    {
                        //过滤掉离线设备
                        m_deviceList.removeIf(p -> p.getM_state() == 0);
                    }
                    //离线设备
                    else
                    {
                        //过滤掉在线设备
                        m_deviceList.removeIf(p -> p.getM_state() == 1);
                    }
                    m_deviceInfoRecyclerAdapter = new DeviceInfoRecyclerAdapter(m_context, m_deviceList);
                    //设置适配器
                    m_recyclerView.setAdapter(m_deviceInfoRecyclerAdapter);
                    SetDeviceInfoRecyclerAdapterListener();
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_context, "请求超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    /**
     * 用OkHttp发送网络请求,并在里面开启线程
     */
    private void SendWithOkHttp()
    {
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
                    Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_FAIL, "请求失败:" + e.toString());
                    handler.sendMessage(message);
                }

                //获得请求响应的字符串:response.body().string()该方法只能被调用一次!另:toString()返回的是对象地址
                //获得请求响应的二进制字节数组:response.body().bytes()
                //获得请求响应的inputStream:response.body().byteStream()
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException
                {
                    String responseStr = response.body().string();
                    Log.i(TAG, "请求响应:" + responseStr);
                    if(response.isSuccessful())
                    {
                        Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_SUCCESS, responseStr);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Message message = handler.obtainMessage(MESSAGE_GETRESPONSE_FAIL, responseStr);
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
        m_bottomNavigationView = getActivity().findViewById(R.id.nav_view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceListView = inflater.inflate(R.layout.fragment_device_list, container, false);
        InitView();
        return m_deviceListView;
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
        m_refreshTimer.cancel();
    }
}
