package com.zistone.blowdown_app.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.blowdown_app.ImageUtil;
import com.zistone.blowdown_app.R;
import com.zistone.blowdown_app.UserSharedPreference;
import com.zistone.blowdown_app.entity.UserInfo;
import com.zistone.blowdown_app.http.OkHttpUtil;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UserInfoFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String URL = "http://192.168.10.197:8080/Blowdown_Web/UserInfo/Update";
    private static final int MESSAGE_GETRESPONSE_SUCCESS = 0;
    private static final int MESSAGE_GETRESPONSE_FAIL = 1;
    //6~12位字母数字组合
    private static final String REGEXUSERNAME = "([a-zA-Z0-9]{6,12})";
    //首位不能是数字,不能全为数字或字母,6~16位
    private static final String REGEXPASSWORD = "^(?![0-9])(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$";
    //本地照片回传
    private static final int CHOOSE_PICTURE = 0;
    //系统相机回传
    private static final int TAKE_PICTURE = 1;
    //裁剪图片回传
    private static final int CROP_SMALL_PICTURE = 2;
    //用户头像
    private Uri m_imageUri;
    private Bitmap m_bitmap;

    private String mParam1;
    private String mParam2;

    private Context m_context;
    private View m_userInfoView;
    private EditText m_editText_userRealName;
    private EditText m_editText_userPhone;
    private EditText m_editText_password;
    private EditText m_editText_rePassword;
    private Button m_btnUpdate;
    private Button m_btnLogout;
    private ProgressBar m_updateUserInfoProgressBar;
    private ImageView m_imageView;

    private OnFragmentInteractionListener mListener;

    public UserInfoFragment()
    {
    }

    public static UserInfoFragment newInstance(String param1, String param2)
    {
        UserInfoFragment fragment = new UserInfoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 将选取的图片设置到ImageView控件
     *
     * @param intent
     */
    private void SetImageToView(Intent intent)
    {
        if(null == intent)
        {
            Log.i("UserInfoFragment", ">>>Intent为Null");
            return;
        }
        Bundle bundle = intent.getExtras();
        m_bitmap = bundle.getParcelable("data");
        if(null == m_bitmap)
        {
            Log.i("UserInfoFragment", ">>>Bitmap为Null");
            return;
        }
        m_imageView.setImageBitmap(m_bitmap);
    }

    /**
     * 更新完成,用于释放控件
     */
    private void IsUpdateEnd()
    {
        m_updateUserInfoProgressBar.setVisibility(View.INVISIBLE);
        m_editText_userRealName.setEnabled(true);
        m_editText_userPhone.setEnabled(true);
        m_editText_password.setEnabled(true);
        m_editText_rePassword.setEnabled(true);
        m_btnUpdate.setEnabled(true);
        m_btnLogout.setEnabled(true);
    }

    /**
     * 正在更新,用于禁止控件
     */
    private void IsUpdateing()
    {
        m_updateUserInfoProgressBar.setVisibility(View.VISIBLE);
        m_editText_userRealName.setEnabled(false);
        m_editText_userPhone.setEnabled(false);
        m_editText_password.setEnabled(false);
        m_editText_rePassword.setEnabled(false);
        m_btnUpdate.setEnabled(false);
        m_btnLogout.setEnabled(false);
    }

    /**
     * 裁剪图片
     *
     * @param uri
     */
    private void CropImage(Uri uri)
    {
        if(null == uri)
        {
            Log.e("UserInfoFragment", ">>>Uri为Null");
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    private void ShowChoosePhotoDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
        builder.setTitle("设置头像");
        String[] items = {"选择本地照片", "拍照"};
        builder.setNegativeButton("取消", null);
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                switch(which)
                {
                    //选择本地照片
                    case CHOOSE_PICTURE:
                    {
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        //如果限制上传到服务器的图片类型:"image/jpeg、image/png"等的类型,所有类型则写"image/*"
                        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(intent, CHOOSE_PICTURE);
                        break;
                    }
                    //拍照
                    case TAKE_PICTURE:
                    {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        m_imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "blowdown_userimage.jpeg"));
                        startActivityForResult(intent, TAKE_PICTURE);
                        break;
                    }
                    default:
                        break;
                }
            }
        });
        builder.create().show();
    }

    /**
     * 动态获取权限
     */
    private void RequestPermission()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            //取消严格模式
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for(String perm : permissions)
            {
                if(PackageManager.PERMISSION_GRANTED != m_context.checkSelfPermission(perm))
                {
                    //进入到这里代表没有权限
                    permissionsList.add(perm);
                }
            }
            //授权
            if(!permissionsList.isEmpty())
            {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
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
                    UserInfo userInfo = JSON.parseObject(responseStr, UserInfo.class);
                    break;
                }
                case MESSAGE_GETRESPONSE_FAIL:
                {
                    String responseStr = (String) message.obj;
                    Toast.makeText(m_context, "请求超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    IsUpdateEnd();
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
                if(null != m_bitmap)
                {
                    byte[] bytes = ImageUtil.BitmapToByteArray(m_bitmap);
                    map.put("m_userImage", Base64.encodeToString(bytes, Base64.DEFAULT));
                }
                map.put("m_realName", m_editText_userRealName.getText().toString());
                map.put("m_phoneNumber", m_editText_userPhone.getText().toString());
                map.put("m_password", m_editText_rePassword.getText().toString());
                OkHttpUtil okHttpUtil = new OkHttpUtil();
                //异步方式发起请求,回调处理信息
                okHttpUtil.AsynSendByPost(URL, map, new Callback()
                {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e)
                    {
                        Log.e("UserInfoFragment", "请求失败:" + e.toString());
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
                        Log.i("UserInfoFragment", "请求响应:" + responseStr);
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

    private void InitData()
    {
        //动态获取权限
        RequestPermission();
    }

    private void InitView()
    {
        m_context = m_userInfoView.getContext();
        m_editText_userRealName = m_userInfoView.findViewById(R.id.editText_userRealName_userInfo);
        m_editText_userRealName.setOnFocusChangeListener(this);
        m_editText_userPhone = m_userInfoView.findViewById(R.id.editText_userPhone_userInfo);
        m_editText_userPhone.setOnFocusChangeListener(this);
        m_editText_password = m_userInfoView.findViewById(R.id.editText_password_userInfo);
        m_editText_password.setOnFocusChangeListener(this);
        m_editText_rePassword = m_userInfoView.findViewById(R.id.editText_rePassword_userInfo);
        m_editText_rePassword.setOnFocusChangeListener(this);
        m_btnUpdate = m_userInfoView.findViewById(R.id.btnUpdate_userInfo);
        m_btnUpdate.setOnClickListener(this);
        m_btnLogout = m_userInfoView.findViewById(R.id.btnLogout_userInfo);
        m_btnLogout.setOnClickListener(this);
        m_imageView = m_userInfoView.findViewById(R.id.imageView);
        m_imageView.setOnClickListener(this);
        m_updateUserInfoProgressBar = m_userInfoView.findViewById(R.id.progressBar_updateUserInfo);
    }

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK)
        {
            return;
        }
        switch(requestCode)
        {
            case CHOOSE_PICTURE:
                CropImage(data.getData());
                break;
            case TAKE_PICTURE:
                CropImage(m_imageUri);
                break;
            case CROP_SMALL_PICTURE:
                SetImageToView(data);
                break;
            default:
                break;
        }
    }

    /**
     * 停止Fragment时被回调
     */
    @Override
    public void onStop()
    {
        super.onStop();
    }

    /**
     * 创建Fragment时回调,只会被调用一次
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /**
     * 绘制Fragment组件时回调
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        m_userInfoView = inflater.inflate(R.layout.fragment_userinfo, container, false);
        InitView();
        InitData();
        return m_userInfoView;
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

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v)
    {
        //选择图片
        if(R.id.imageView == v.getId())
        {
            ShowChoosePhotoDialog();
        }
        //更新信息
        else if(R.id.btnUpdate_userInfo == v.getId())
        {
            IsUpdateing();
            SendWithOkHttp();
        }
        //退出登录
        else if(R.id.btnLogout_userInfo == v.getId())
        {
            UserSharedPreference.LogoutSuccess(m_context);
            //用户碎片的子碎片切换至登录碎片
            List<Fragment> fragmentList = getFragmentManager().getFragments();
            for(Fragment fragment : fragmentList)
            {
                //注意:一个FragmentTransaction只能Commit一次,不要用全局或共享一个FragmentTransaction对象,多个Fragment则多次get
                if(!"loginFragment".equals(fragment.getTag()))
                {
                    getFragmentManager().beginTransaction().hide(fragment).commitNow();
                }
                else
                {
                    getFragmentManager().beginTransaction().show(fragment).commitNow();
                }
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(R.id.editText_userRealName_userInfo == v.getId())
        {
            if(hasFocus)
            {
                m_editText_userRealName.setError(null);
            }
            else
            {
                if(Pattern.matches(REGEXUSERNAME, m_editText_userRealName.getText().toString()))
                {
                    m_editText_userRealName.setError(null);
                }
                else
                {
                    m_editText_userRealName.setError("姓名非法");
                }
            }
        }
        else if(R.id.editText_userPhone_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
        else if(R.id.editText_password_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
        else if(R.id.editText_rePassword_userInfo == v.getId())
        {
            if(hasFocus)
            {
            }
            else
            {
            }
        }
    }

}
