package com.zistone.gprs.activity;

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

import com.baidu.mapapi.map.MapFragment;
import com.zistone.gprs.R;
import com.zistone.gprs.fragment.MapFragment_Bind;
import com.zistone.gprs.fragment.MapFragment_Choose;
import com.zistone.gprs.fragment.DeviceFragment_Info;
import com.zistone.gprs.fragment.MapFragment_Map;
import com.zistone.gprs.fragment.MapFragment_Setting;
import com.zistone.gprs.fragment.MapFragment_TrackQuery;
import com.zistone.gprs.fragment.UserFragment_Forget;
import com.zistone.gprs.fragment.UserFragment_Info;
import com.zistone.gprs.fragment.UserFragment_Login;
import com.zistone.gprs.fragment.UserFragment_Register;
import com.zistone.gprs.util.UserSharedPreference;
import com.zistone.gprs.fragment.DeviceFragment_Add;
import com.zistone.gprs.fragment.DeviceFragment;
import com.zistone.gprs.fragment.DeviceFragment_List;
import com.zistone.gprs.fragment.DeviceFragment_Manage;
import com.zistone.gprs.fragment.UserFragment;

import java.io.Serializable;

public class MainActivity extends AppCompatActivity implements
        //位置
        com.zistone.gprs.fragment.MapFragment.OnFragmentInteractionListener,
        MapFragment_Map.OnFragmentInteractionListener,
        MapFragment_Bind.OnFragmentInteractionListener,
        MapFragment_Choose.OnFragmentInteractionListener,
        MapFragment_TrackQuery.OnFragmentInteractionListener,
        MapFragment_Setting.OnFragmentInteractionListener,
        //设备
        DeviceFragment.OnFragmentInteractionListener,
        DeviceFragment_Info.OnFragmentInteractionListener,
        DeviceFragment_Manage.OnFragmentInteractionListener,
        DeviceFragment_List.OnFragmentInteractionListener,
        DeviceFragment_Add.OnFragmentInteractionListener,
        //用户
        UserFragment.OnFragmentInteractionListener,
        UserFragment_Login.OnFragmentInteractionListener,
        UserFragment_Register.OnFragmentInteractionListener,
        UserFragment_Forget.OnFragmentInteractionListener,
        UserFragment_Info.OnFragmentInteractionListener
{
    //当前页,用来切换
    public Fragment _currentFragment;
    //地图页
    public com.zistone.gprs.fragment.MapFragment _mapFragment;
    //设备页
    public DeviceFragment _deviceFragment;
    //用户页
    public UserFragment _userFragment;
    //底部导航栏
    public BottomNavigationView _bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitData();
    }

    private void InitData()
    {
        _bottomNavigationView = findViewById(R.id.nav_view);
        //启动时如果已经登录过则在设备页,否则跳转至用户页的登录界面
        String realName = UserSharedPreference.GetRealName(this);
        int state = UserSharedPreference.GetState(this);
        if (!"".equals(realName) && 1 == state)
        {
            _deviceFragment = DeviceFragment.newInstance("", "");
            _bottomNavigationView.setSelectedItemId(_bottomNavigationView.getMenu().getItem(1).getItemId());
            _currentFragment = _deviceFragment;
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, _currentFragment, "deviceFragment").show(_currentFragment).commitNow();
        }
        else
        {
            _userFragment = UserFragment.newInstance("", "");
            _bottomNavigationView.setSelectedItemId(_bottomNavigationView.getMenu().getItem(2).getItemId());
            _currentFragment = _userFragment;
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_current, _currentFragment, "userFragment").show(_currentFragment).commitNow();
        }
        _bottomNavigationView.setOnNavigationItemSelectedListener(OnNavigationItemSeletecListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener OnNavigationItemSeletecListener = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
        {
            switch (menuItem.getItemId())
            {
                case R.id.navigation_map:
                    ClickMapItem();
                    return true;
                //如果已经登录过则允许跳转至设备页,否则跳转至用户页的登录界面
                case R.id.navigation_device:
                    String realName = UserSharedPreference.GetRealName(getApplicationContext());
                    int state = UserSharedPreference.GetState(getApplicationContext());
                    if (!"".equals(realName) && 1 == state)
                    {
                        ClickDeviceItem();
                    }
                    else
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setPositiveButton("确定", (dialog, which) ->
                        {
                            dialog.dismiss();
                            _bottomNavigationView.setSelectedItemId(_bottomNavigationView.getMenu().getItem(2).getItemId());
                        });
                        builder.setMessage("你还没有登录");
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
        if (_userFragment == null)
        {
            _userFragment = UserFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), _userFragment, "userFragment");
    }

    private void ClickDeviceItem()
    {
        if (_deviceFragment == null)
        {
            _deviceFragment = DeviceFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), _deviceFragment, "deviceFragment");
    }

    private void ClickMapItem()
    {
        _mapFragment = (com.zistone.gprs.fragment.MapFragment) getSupportFragmentManager().findFragmentByTag("mapFragment");
        if (_mapFragment == null)
        {
            _mapFragment = com.zistone.gprs.fragment.MapFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), _mapFragment, "mapFragment");
    }

    private void AddOrShowFragment(FragmentTransaction transaction, Fragment fragment, String tagStr)
    {
        if (_currentFragment == null)
        {
            return;
        }
        //如果当前的Fragment未被添加到管理器中
        if (!fragment.isAdded())
        {
            transaction.hide(_currentFragment).add(R.id.fragment_current, fragment, tagStr).commitNow();
        }
        //否则就显示
        else
        {
            transaction.hide(_currentFragment).show(fragment).commitNow();
        }
        _currentFragment = fragment;
    }

    @Override
    public void onFragmentInteraction(Uri uri)
    {
        Toast.makeText(this, "----------DuangDuangDuang------------", Toast.LENGTH_LONG).show();
    }
}
