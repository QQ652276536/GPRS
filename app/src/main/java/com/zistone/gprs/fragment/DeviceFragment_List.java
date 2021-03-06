package com.zistone.gprs.fragment;

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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;
import com.zistone.gprs.control.DeviceInfoRecyclerAdapter;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

public class DeviceFragment_List extends Fragment implements View.OnClickListener
{
    private static final String TAG = "DeviceFragment_List";
    private static final String ARG_PARAM1 = "DEVICESTATE";
    private static final String ARG_PARAM2 = "param2";
    private static final int TIMEINTERVAL = 30 * 1000;
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
    private Context _context;
    private View _deviceListView;
    private List<DeviceInfo> _deviceList = new ArrayList<>();
    //下拉刷新控件
    private MaterialRefreshLayout _materialRefreshLayout;
    //RecyclerView
    private RecyclerView _recyclerView;
    //适配器
    private DeviceInfoRecyclerAdapter _deviceInfoRecyclerAdapter;
    private Timer _refreshTimer;
    //设备状态
    private int _deviceState;
    private TextView _ToolbarTextView;
    private ImageButton _btnReturn;
    //底部导航栏
    public BottomNavigationView _bottomNavigationView;
    private OnFragmentInteractionListener _listener;

    /**
     * @param param1 设备状态
     * @param param2
     * @return
     */
    public static DeviceFragment_List newInstance(int param1, String param2)
    {
        DeviceFragment_List fragment = new DeviceFragment_List();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return_device_list:
                DeviceFragment_Manage deviceFragment_manage = DeviceFragment_Manage.newInstance("", "");
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_manage, "deviceFragment_manage").commitNow();
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

    /**
     * 定时刷新设备列表
     */
    private void RefreshDeviceList()
    {
        _refreshTimer = new Timer();
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
        //_refreshTimer.schedule(refreshTask, 0, TIMEINTERVAL);
    }

    /**
     * 给下拉控件设置点击事件
     */
    private void SetDeviceInfoRecyclerAdapterListener()
    {
        if(null != _deviceInfoRecyclerAdapter)
        {
            _deviceInfoRecyclerAdapter.SetOnItemClickListener(new DeviceInfoRecyclerAdapter.OnItemClickListener()
            {
                @Override
                public void OnClick(int position)
                {
                    DeviceInfo tempDevice = _deviceList.get(position);
                    DeviceFragment_Info deviceFragment_info = DeviceFragment_Info.newInstance(tempDevice);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_device, deviceFragment_info, "deviceFragment_info").commitNow();
                }

                @Override
                public void OnLongClick(int position)
                {
                    Toast.makeText(_context, "当前长按 " + position, Toast.LENGTH_SHORT).show();
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
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "网络连接超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    if(null == result || "".equals(result))
                    {
                        return;
                    }
                    _deviceList = JSON.parseArray(result, DeviceInfo.class);
                    Iterator<DeviceInfo> iterator = _deviceList.iterator();
                    //在线设备
                    if(_deviceState == 1)
                    {
                        //过滤掉离线设备
                        while(iterator.hasNext())
                        {
                            DeviceInfo temp = iterator.next();
                            if(temp.getState() == 0)
                            {
                                iterator.remove();
                            }
                        }
                    }
                    //离线设备
                    else
                    {
                        //过滤掉在线设备
                        while(iterator.hasNext())
                        {
                            DeviceInfo temp = iterator.next();
                            if(temp.getState() == 1)
                            {
                                iterator.remove();
                            }
                        }
                    }
                    _deviceInfoRecyclerAdapter = new DeviceInfoRecyclerAdapter(_context, _deviceList);
                    //设置适配器
                    _recyclerView.setAdapter(_deviceInfoRecyclerAdapter);
                    SetDeviceInfoRecyclerAdapterListener();
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

    private void SendWithOkHttp()
    {
        new Thread(() ->
        {
            Looper.prepare();
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create("", MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //异步请求
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "查询所有设备失败:" + e.toString());
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
                        Log.i(TAG, "查询所有设备成功:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "查询所有设备失败:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, result);
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
        _bottomNavigationView = getActivity().findViewById(R.id.nav_view);
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
        _deviceListView = inflater.inflate(R.layout.fragment_device_list, container, false);
        _context = _deviceListView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/FindAll";
        _btnReturn = _deviceListView.findViewById(R.id.btn_return_device_list);
        _btnReturn.setOnClickListener(this::onClick);
        _ToolbarTextView = _deviceListView.findViewById(R.id.textView_toolbar);
        _recyclerView = _deviceListView.findViewById(R.id.device_recycler);
        _deviceState = getArguments().getInt("DEVICESTATE");
        if(_deviceState == 1)
        {
            _ToolbarTextView.setText("可用设备列表");
        }
        else
        {
            _ToolbarTextView.setText("停用设备列表");
        }
        //下拉刷新控件
        _materialRefreshLayout = _deviceListView.findViewById(R.id.refresh);
        //启用加载更多
        _materialRefreshLayout.setLoadMore(true);
        _materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener()
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
                //Toast.makeText(_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             * @param materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout materialRefreshLayout)
            {
                //Toast.makeText(_context, "别滑了,到底了", Toast.LENGTH_SHORT).show();
            }
        });
        //使用线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(_context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //此时的_deviceList里面并没有数据,但是需要设置适配器,可以避免在第一次自动下拉刷新时抛出No adapter attached; skipping layout异常
        _deviceInfoRecyclerAdapter = new DeviceInfoRecyclerAdapter(_context, _deviceList);
        //设置适配器
        _recyclerView.setAdapter(_deviceInfoRecyclerAdapter);
        SetDeviceInfoRecyclerAdapterListener();
        //设置布局
        _recyclerView.setLayoutManager(linearLayoutManager);
        //自动刷新
        _materialRefreshLayout.autoRefresh();
        //定时刷新
        RefreshDeviceList();
        return _deviceListView;
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
        _refreshTimer.cancel();
    }
}
