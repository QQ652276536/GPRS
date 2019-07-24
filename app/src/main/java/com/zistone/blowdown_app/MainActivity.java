package com.zistone.blowdown_app;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.fragment.DeviceFragment;
import com.zistone.blowdown_app.fragment.ForgetFragment;
import com.zistone.blowdown_app.fragment.LoginFragment;
import com.zistone.blowdown_app.fragment.MapFragment;
import com.zistone.blowdown_app.fragment.RegisterFragment;
import com.zistone.blowdown_app.fragment.UserFragment;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener, DeviceFragment.OnFragmentInteractionListener, UserFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener, ForgetFragment.OnFragmentInteractionListener, Serializable
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
        //启动时如果已经登录过则在设备页,否则在用户页的登录界面
        if(!"".equals(UserSharedPreference.GetRealName(this)) && 1 == UserSharedPreference.GetState(this))
        {
            m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(1).getItemId());
            //实例化设备页
            if(m_deviceFragment == null)
            {
                m_deviceFragment = DeviceFragment.newInstance(this, "");
            }
            //该碎片不在管理器中则添加进去
            if(!m_deviceFragment.isAdded())
            {
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_deviceFragment).commitAllowingStateLoss();
            }
            else
            {
                getSupportFragmentManager().beginTransaction().show(m_deviceFragment).commitAllowingStateLoss();
            }
            m_currentFragment = m_deviceFragment;
        }
        else
        {
            m_bottomNavigationView.setSelectedItemId(m_bottomNavigationView.getMenu().getItem(2).getItemId());
            if(m_userFragment == null)
            {
                m_userFragment = UserFragment.newInstance("", "");
            }
            if(!m_userFragment.isAdded())
            {
                getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_userFragment).commitAllowingStateLoss();
            }
            else
            {
                getSupportFragmentManager().beginTransaction().show(m_userFragment).commitAllowingStateLoss();
            }
            m_currentFragment = m_userFragment;
        }
        //因为启动时有默认选中页,防止事件重复触发,所以在后面注册监听事件
        m_bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSeletecListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSeletecListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch(menuItem.getItemId())
            {
                case R.id.navigation_map:
                    ClickMapItem();
                    return true;
                case R.id.navigation_device:
                    ClickDeviceItem();
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
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_userFragment);
    }

    private void ClickDeviceItem()
    {
        if(m_deviceFragment == null)
        {
            m_deviceFragment = DeviceFragment.newInstance(this, "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_deviceFragment);
    }

    private void ClickMapItem()
    {
        if(m_mapFragment == null)
        {
            m_mapFragment = MapFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_mapFragment);
    }

    private void AddOrShowFragment(FragmentTransaction transaction, Fragment fragment)
    {
        if(m_currentFragment == null)
        {
            return;
        }
        //如果当前的Fragment未被添加到管理器中
        if(!fragment.isAdded())
        {
            transaction.hide(m_currentFragment).add(R.id.fragment_current, fragment).commitAllowingStateLoss();
        }
        //否则就显示
        else
        {
            transaction.hide(m_currentFragment).show(fragment).commitAllowingStateLoss();
        }
        m_currentFragment = fragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Toast.makeText(this, "----------DuangDuangDuang------------", Toast.LENGTH_LONG).show();
    }
}
