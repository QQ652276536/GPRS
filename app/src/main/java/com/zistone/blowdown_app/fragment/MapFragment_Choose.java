package com.zistone.blowdown_app.fragment;

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
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.DeviceInfoChooseRecyclerAdapter;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.util.PropertiesUtil;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
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
    private Context m_context;
    private View m_deviceListView;
    private List<DeviceInfo> m_deviceList = new ArrayList<>();
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    //RecyclerView
    private RecyclerView m_recyclerView;
    //适配器
    private DeviceInfoChooseRecyclerAdapter m_deviceInfoChooseRecyclerAdapter;
    private ImageButton m_btnReturn;
    private OnFragmentInteractionListener mListener;
    private DeviceInfo m_deviceInfo;

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
        switch (v.getId())
        {
            case R.id.btn_return_device_choose:
                //重新实例化地图碎片实现重新加载设备位置
                MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(m_deviceInfo);
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
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = m_deviceListView.getContext();
        URL = PropertiesUtil.GetValueProperties(m_context).getProperty("URL") + "/DeviceInfo/FindAll";
        m_btnReturn = m_deviceListView.findViewById(R.id.btn_return_device_choose);
        m_btnReturn.setOnClickListener(this::onClick);
        m_recyclerView = m_deviceListView.findViewById(R.id.device_recycler_choose);
        //下拉刷新控件
        m_materialRefreshLayout = m_deviceListView.findViewById(R.id.refresh_choose);
        //启用加载更多
        m_materialRefreshLayout.setLoadMore(true);
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
                Toast.makeText(m_context, "别滑了,到底了", Toast.LENGTH_SHORT).show();
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
    }

    /**
     * 给下拉控件设置事件
     */
    private void SetDeviceInfoRecyclerAdapterListener()
    {
        if (null != m_deviceInfoChooseRecyclerAdapter)
        {
            m_deviceInfoChooseRecyclerAdapter.SetOnItemClickListener(new DeviceInfoChooseRecyclerAdapter.OnItemClickListener()
            {
                @Override
                public void OnClick(int position)
                {
                    m_deviceInfo = m_deviceList.get(position);
                    //重新实例化地图碎片实现重新加载设备位置
                    MapFragment_Map mapFragment_map = MapFragment_Map.newInstance(m_deviceInfo);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_current_map, mapFragment_map, "mapFragment_map").show(mapFragment_map).commitNow();
                }

                @Override
                public void OnLongClick(int position)
                {
                    //Toast.makeText(m_context, "当前长按 " + position, Toast.LENGTH_SHORT).show();
                }
            });
            m_deviceInfoChooseRecyclerAdapter.SetOnClickListener(position ->
            {
                m_deviceInfo = m_deviceList.get(position);
                if (null == m_deviceInfo)
                {
                    return;
                }
                MapFragment_Bind mapFragment_bind = MapFragment_Bind.newInstance(m_deviceInfo);
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
                    m_deviceList = JSON.parseArray(result, DeviceInfo.class);
                    //过滤掉离线设备
                    m_deviceList.removeIf(p -> p.getM_state() == 0);
                    m_deviceInfoChooseRecyclerAdapter = new DeviceInfoChooseRecyclerAdapter(m_context, m_deviceList);
                    //设置适配器
                    m_recyclerView.setAdapter(m_deviceInfoChooseRecyclerAdapter);
                    SetDeviceInfoRecyclerAdapterListener();
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
                    Log.i(TAG, "响应内容:" + result);
                    if (response.isSuccessful())
                    {
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
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
        if (getArguments() != null)
        {
            //获取设备信息
            m_deviceInfo = getArguments().getParcelable("DEVICEINFO");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_deviceListView = inflater.inflate(R.layout.fragment_map_device_choose, container, false);
        InitView();
        return m_deviceListView;
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
