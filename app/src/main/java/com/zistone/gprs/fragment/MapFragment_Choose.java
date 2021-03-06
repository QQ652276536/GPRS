package com.zistone.gprs.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.gprs.R;
import com.zistone.gprs.control.DeviceInfoChooseRecyclerAdapter;
import com.zistone.gprs.pojo.DeviceInfo;
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

public class MapFragment_Choose extends Fragment implements View.OnClickListener
{
    private static final String TAG = "MapFragment_Choose";
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
    private DeviceInfoChooseRecyclerAdapter _deviceInfoChooseRecyclerAdapter;
    private ImageButton _btnReturn;
    private OnFragmentInteractionListener _listener;
    private DeviceInfo _deviceInfo;

    /**
     * @param deviceInfo
     * @return
     */
    public static MapFragment_Choose newInstance(DeviceInfo deviceInfo)
    {
        MapFragment_Choose fragment = new MapFragment_Choose();
        Bundle args = new Bundle();
        args.putParcelable("DEVICEINFO", deviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            case R.id.btn_return_device_choose:
                MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").show(mapFragment_map).commitNow();
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
     * 给下拉控件设置事件
     */
    private void SetDeviceInfoRecyclerAdapterListener()
    {
        if(null != _deviceInfoChooseRecyclerAdapter)
        {
            _deviceInfoChooseRecyclerAdapter.SetOnItemClickListener(new DeviceInfoChooseRecyclerAdapter.OnItemClickListener()
            {
                @Override
                public void OnClick(int position)
                {
                    _deviceInfo = _deviceList.get(position);
                    MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(_deviceInfo);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").show(mapFragment_map).commitNow();
                }

                @Override
                public void OnLongClick(int position)
                {
                    //Toast.makeText(_context, "当前长按 " + position, Toast.LENGTH_SHORT).show();
                }
            });
            _deviceInfoChooseRecyclerAdapter.SetOnClickListener(position ->
            {
                _deviceInfo = _deviceList.get(position);
                if(null == _deviceInfo)
                {
                    return;
                }
                MapFragment_Bind mapFragment_bind = MapFragment_Bind.newInstance(_deviceInfo);
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_bind, "mapFragment_bind").commitNow();
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
                    //过滤掉离线设备
                    while(iterator.hasNext())
                    {
                        DeviceInfo temp = iterator.next();
                        if(temp.getState() == 0)
                        {
                            iterator.remove();
                        }
                    }
                    _deviceInfoChooseRecyclerAdapter = new DeviceInfoChooseRecyclerAdapter(_context, _deviceList);
                    //设置适配器
                    _recyclerView.setAdapter(_deviceInfoChooseRecyclerAdapter);
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
                    Log.e(TAG, "查询可用设备失败:" + e.toString());
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
                        Log.i(TAG, "查询可用设备成功:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "查询可用设备失败:" + result);
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
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            //获取设备信息
            _deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        _deviceListView = inflater.inflate(R.layout.fragment_map_device_choose, container, false);
        _context = _deviceListView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/DeviceInfo/FindAll";
        _btnReturn = _deviceListView.findViewById(R.id.btn_return_device_choose);
        _btnReturn.setOnClickListener(this::onClick);
        _recyclerView = _deviceListView.findViewById(R.id.device_recycler_choose);
        //下拉刷新控件
        _materialRefreshLayout = _deviceListView.findViewById(R.id.refresh_choose);
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
        _deviceInfoChooseRecyclerAdapter = new DeviceInfoChooseRecyclerAdapter(_context, _deviceList);
        //设置适配器
        _recyclerView.setAdapter(_deviceInfoChooseRecyclerAdapter);
        SetDeviceInfoRecyclerAdapterListener();
        //设置布局
        _recyclerView.setLayoutManager(linearLayoutManager);
        //自动刷新
        _materialRefreshLayout.autoRefresh();
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
    }
}
