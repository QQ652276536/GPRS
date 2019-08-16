package com.zistone.blowdown_app;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.zistone.blowdown_app.entity.DeviceInfo;
import com.zistone.blowdown_app.fragment.DeviceFragment;
import com.zistone.blowdown_app.fragment.ForgetFragment;
import com.zistone.blowdown_app.fragment.LoginFragment;
import com.zistone.blowdown_app.fragment.MapFragment;
import com.zistone.blowdown_app.fragment.RegisterFragment;
import com.zistone.blowdown_app.fragment.UserFragment;
import com.zistone.blowdown_app.fragment.UserInfoFragment;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener, DeviceFragment.OnFragmentInteractionListener, UserFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener, ForgetFragment.OnFragmentInteractionListener, UserInfoFragment.OnFragmentInteractionListener, Serializable
{
    //当前页,用来切换
    public Fragment m_currentFragment;
    //地图页
    public MapFragment m_mapFragment;
    //设备页
    public DeviceFragment m_deviceFragment;
    //用户页
    public UserFragment m_userFragment;
    //底部导航栏
    public BottomNavigationView m_bottomNavigationView;
    //点击设备列表显示对应设备位置信息时需要将底部导航栏的选中样式修改,也使用了选中事件,避免重复触发
    public boolean m_isFromClickDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
    }

    private void InitData()
    {
        m_bottomNavigationView = findViewById(R.id.nav_view);
        //启动时如果已经登录过则在设备页,否则跳转至用户页的登录界面
        String realName = UserSharedPreference.GetRealName(this);
        int state = UserSharedPreference.GetState(this);
        if(!"".equals(realName) && 1 == state)
        {
            m_currentFragment = DeviceFragment.newInstance();
            m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(1).getItemId());
        }
        else
        {
            m_currentFragment = UserFragment.newInstance("", "");
            m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(2).getItemId());
        }
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_currentFragment).show(m_currentFragment).commitNow();
        //因为启动时有默认选中页,防止事件重复触发,所以在后面注册监听事件
        m_bottomNavigationView.setOnNavigationItemSelectedListener(OnNavigationItemSeletecListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSeletecListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch(menuItem.getItemId())
            {
                case R.id.navigation_map:
                    if(!m_isFromClickDevice)
                    {
                        ClickMapItem();
                        m_isFromClickDevice = true;
                    }
                    return true;
                //如果已经登录过则允许跳转至设备页,否则跳转至用户页的登录界面
                case R.id.navigation_device:
                    String realName = UserSharedPreference.GetRealName(getApplicationContext());
                    int state = UserSharedPreference.GetState(getApplicationContext());
                    if(!"".equals(realName) && 1 == state)
                    {
                        ClickDeviceItem();
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                //ClickUserItem();
                                m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(2).getItemId());
                            }
                        });
                        builder.setMessage("您还没有登录");
                        builder.show();
                    }
                    return true;
                case R.id.navigation_user:
                    ClickUserItem();
                    return true;
            }
            return false;
        }
    };

    private void ClickUserItem()
    {
        if(m_userFragment == null)
        {
            m_userFragment = UserFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_userFragment, "userFragment");
    }

    private void ClickDeviceItem()
    {
        if(m_deviceFragment == null)
        {
            m_deviceFragment = DeviceFragment.newInstance();
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_deviceFragment, "deviceFragment");
    }

    private void ClickMapItem()
    {
        if(m_mapFragment == null)
        {
            m_mapFragment = MapFragment.newInstance(null);
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_mapFragment, "mapFragment");
    }

    private void AddOrShowFragment(FragmentTransaction transaction, Fragment fragment, String tagStr)
    {
        if(m_currentFragment == null)
        {
            return;
        }
        //如果当前的Fragment未被添加到管理器中
        if(!fragment.isAdded())
        {
            transaction.hide(m_currentFragment).add(R.id.fragment_current, fragment, tagStr).commitNow();
        }
        //否则就显示
        else
        {
            transaction.hide(m_currentFragment).show(fragment).commitNow();
        }
        m_currentFragment = fragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Toast.makeText(this, "----------DuangDuangDuang------------", Toast.LENGTH_LONG).show();
    }
}
