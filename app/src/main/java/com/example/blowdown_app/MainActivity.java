package com.example.blowdown_app;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private Fragment m_currentFragment;
    private Fragment m_mapFragment;
    private DeviceFragment m_deviceFragment;
    private UserFragment m_userFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSeletecListener);
        InitData();
    }

    private void InitData() {
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSeletecListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
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

    private void ClickUserItem() {
    }

    private void ClickDeviceItem() {
    }

    private void ClickMapItem() {
        if (m_mapFragment == null) {
            m_mapFragment = MapFragment.newInstance("", "");
        }
        AddOrShowFragment(getSupportFragmentManager().beginTransaction(), m_mapFragment);
    }

    private void AddOrShowFragment(FragmentTransaction transaction, Fragment fragment) {
        if (m_currentFragment == null) {
            return;
        }
        //如果当前的Fragment未被添加到管理器中
        if (!fragment.isAdded()) {
            transaction.hide(m_currentFragment).add(R.id.fragment_container, fragment).commitAllowingStateLoss();
        }
        //否则就显示
        else {
            transaction.hide(m_currentFragment).show(fragment).commitAllowingStateLoss();
        }
    }

}
