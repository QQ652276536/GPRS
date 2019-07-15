package com.example.blowdown_app;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.blowdown_app.fragment.DeviceFragment;
import com.example.blowdown_app.fragment.ForgetFragment;
import com.example.blowdown_app.fragment.LoginFragment;
import com.example.blowdown_app.fragment.MapFragment;
import com.example.blowdown_app.fragment.RegisterFragment;
import com.example.blowdown_app.fragment.UserFragment;

public class MainActivity extends AppCompatActivity implements MapFragment.OnFragmentInteractionListener, DeviceFragment.OnFragmentInteractionListener
        , UserFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener, RegisterFragment.OnFragmentInteractionListener
        , ForgetFragment.OnFragmentInteractionListener
{
    //当前页,用来切换
    private Fragment m_currentFragment;
    //地图页
    private MapFragment m_mapFragment;
    //设备页
    private DeviceFragment m_deviceFragment;
    //用户页
    private UserFragment m_userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
    }

    private void InitData()
    {
        //底部导航栏
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        //启动时默认在用户页
        bottomNavigationView.setSelectedItemId(bottomNavigationView.getMenu().getItem(2).getItemId());
        //实例化用户页
        if (m_userFragment == null)
        {
            m_userFragment = UserFragment.newInstance("", "");
        }
        //该碎片不在管理器中则添加进去
        if (!m_userFragment.isAdded())
        {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, m_userFragment).commitAllowingStateLoss();
        }
        else
        {
            getSupportFragmentManager().beginTransaction().show(m_userFragment).commitAllowingStateLoss();
        }
        m_currentFragment = m_userFragment;
        //因为启动时默认选中用户页,所以要在后面注册监听事件
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSeletecListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSeletecListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch (menuItem.getItemId())
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
        if (m_userFragment == null)
        {
            m_userFragment = UserFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_userFragment);
    }

    private void ClickDeviceItem()
    {
        if (m_deviceFragment == null)
        {
            m_deviceFragment = DeviceFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_deviceFragment);
    }

    private void ClickMapItem()
    {
        if (m_mapFragment == null)
        {
            m_mapFragment = MapFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_mapFragment);
    }

    private void AddOrShowFragment(FragmentTransaction transaction, Fragment fragment)
    {
        if (m_currentFragment == null)
        {
            return;
        }
        //如果当前的Fragment未被添加到管理器中
        if (!fragment.isAdded())
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
        Toast.makeText(this, "----------------------------", Toast.LENGTH_LONG).show();
    }
}
