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
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.zistone.blowdown_app.MainActivity;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.control.DeviceInfoRecyclerAdapter;
import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.http.OkHttpUtil;
import com.zistone.material_refresh_layout.MaterialRefreshLayout;
import com.zistone.material_refresh_layout.MaterialRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DeviceFragment extends Fragment
{
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    private static final String URL = "http://10.0.2.2:8080/Blowdown_Web/DeviceInfo/FindAll";
    private Context m_context;
    private View m_deviceView;
    private List<DeviceInfo> m_deviceList = new ArrayList<>();
    private MainActivity m_mainActivity;
    //下拉刷新控件
    private MaterialRefreshLayout m_materialRefreshLayout;
    //RecyclerView
    private RecyclerView m_recyclerView;
    //适配器
    private DeviceInfoRecyclerAdapter m_deviceInfoRecyclerAdapter;

    private OnFragmentInteractionListener mListener;

    public DeviceFragment()
    {
    }

    /**
     * 使用工厂方法创建新的实例
     *
     * @param activity
     * @param param2
     * @return
     */
    public static DeviceFragment newInstance(MainActivity activity, String param2)
    {
        DeviceFragment fragment = new DeviceFragment();
        Bundle args = new Bundle();
        args.putSerializable("MainActivityClass", activity);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Activity中加载Fragment时会要求实现onFragmentInteraction(Uri uri)方法,此方法主要作用是从fragment向activity传递数据
     */
    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri)
    {
        if(mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void InitView()
    {
        m_context = m_deviceView.getContext();
        m_mainActivity = (MainActivity) getArguments().getSerializable("MainActivityClass");
        //下拉刷新控件
        m_materialRefreshLayout = m_deviceView.findViewById(R.id.refresh);
        m_materialRefreshLayout.setLoadMore(true);
        //结束上拉刷新
        m_materialRefreshLayout.finishRefreshLoadMore();
        m_materialRefreshLayout.setMaterialRefreshListener(new MaterialRefreshListener()
        {
            /**
             * 下拉刷新
             * @param m_materialRefreshLayout
             */
            @Override
            public void onRefresh(final MaterialRefreshLayout m_materialRefreshLayout)
            {
                m_materialRefreshLayout.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //结束下拉刷新
                        m_materialRefreshLayout.finishRefresh();
                    }
                }, 3000);
            }

            /**
             * 加载完毕
             */
            @Override
            public void onfinish()
            {
                Toast.makeText(m_context, "完成", Toast.LENGTH_LONG).show();
            }

            /**
             * 加载更多
             * @param m_materialRefreshLayout
             */
            @Override
            public void onRefreshLoadMore(MaterialRefreshLayout m_materialRefreshLayout)
            {
                Toast.makeText(m_context, "别滑了,到底了", Toast.LENGTH_LONG).show();
            }
        });
        m_recyclerView = m_deviceView.findViewById(R.id.device_recycler);
        //使用线性布局
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(m_context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        //设置布局
        m_recyclerView.setLayoutManager(linearLayoutManager);
        SendWithOkHttp();

        //            @Override
        //            public void OnItemClick(RecyclerView.ViewHolder viewHolder)
        //            {
        //                DeviceInfo tempDevice = m_deviceList.get(viewHolder.getLayoutPosition());
        //                Toast.makeText(m_context, tempDevice.getM_deviceName(), Toast.LENGTH_SHORT).show();
        //                int a = m_mainActivity.getSupportFragmentManager().getFragments().size();
        //                m_mainActivity.m_deviceInfo = tempDevice;
        //                //重新实例化地图碎片以达到重新加载设备位置
        //                MapFragment mapFragment = MapFragment.newInstance(tempDevice, "");
        //                Fragment currentFragment = getFragmentManager().findFragmentByTag("deviceFragment");
        //                if(m_mainActivity.m_mapFragment == null)
        //                {
        //                    m_mainActivity.m_mapFragment = mapFragment;
        //                    getFragmentManager().beginTransaction().hide(currentFragment).commitAllowingStateLoss();
        //                }
        //                else
        //                {
        //                    Fragment oldMapFragment = getFragmentManager().findFragmentByTag("mapFragment");
        //                    getFragmentManager().beginTransaction().remove(oldMapFragment).commitAllowingStateLoss();
        //                    //getFragmentManager().beginTransaction().replace(R.id.fragment_current, mapFragment).commitAllowingStateLoss();
        //                    m_mainActivity.m_mapFragment = null;
        //                }
        //                m_mainActivity.m_bottomNavigationView.setSelectedItemId(m_mainActivity.m_bottomNavigationView.getMenu().getItem(0).getItemId());
        //            }

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
                    //不同环境SimpleDateFormat模式取到的字符串不一样
                    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
                    m_deviceList = gson.fromJson(responseStr, new TypeToken<List<DeviceInfo>>()
                    {
                    }.getType());
                    //设置适配器
                    m_deviceInfoRecyclerAdapter = new DeviceInfoRecyclerAdapter(m_context, m_deviceList);
                    m_recyclerView.setAdapter(m_deviceInfoRecyclerAdapter);
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_deviceView.getContext(), "请求超时,请检查网络环境", Toast.LENGTH_SHORT).show();
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
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                Looper.prepare();
                Map<String, String> map = new HashMap<>();
                OkHttpUtil okHttpUtil = new OkHttpUtil();
                //异步方式发起请求
                okHttpUtil.AsynSendByPost(URL, map, new Callback()
                {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e)
                    {
                        Log.e("DeviceLog", "请求失败:" + e.toString());
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
                        Log.i("DeviceLog", "请求响应:" + responseStr);
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
            }
        }).start();
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
        m_deviceView = inflater.inflate(R.layout.fragment_device, container, false);
        InitView();
        return m_deviceView;
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
