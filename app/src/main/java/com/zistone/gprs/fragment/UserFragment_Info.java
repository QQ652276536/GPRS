package com.zistone.gprs.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.zistone.gprs.util.ImageUtil;
import com.zistone.gprs.util.PropertiesUtil;
import com.zistone.gprs.R;
import com.zistone.gprs.util.UserSharedPreference;
import com.zistone.gprs.pojo.UserInfo;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserFragment_Info extends Fragment implements View.OnClickListener, View.OnFocusChangeListener
{
    private static final String TAG = "UserFragment_Info";
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final int MESSAGE_RREQUEST_FAIL = 1;
    private static final int MESSAGE_RESPONSE_FAIL = 2;
    private static final int MESSAGE_RESPONSE_SUCCESS = 3;
    private static String URL;
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
    private String _param1;
    private String _param2;
    private Context _context;
    private View _userInfoView;
    private Button _btnUpdate;
    private Button _btnLogout;
    private ProgressBar _progressBar;
    private Uri _imageUri;
    private OnFragmentInteractionListener _listener;
    private ImageView _imageView;
    private EditText _edtPwd;
    private EditText _edtRePwd;
    private EditText _edtUserName;
    private EditText _edtUserRealName;
    private EditText _edtUserPhone;

    public static UserFragment_Info newInstance(String param1, String param2)
    {
        UserFragment_Info fragment = new UserFragment_Info();
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
            Log.e(TAG, ">>>Intent为Null");
            return;
        }
        Bundle bundle = intent.getExtras();
        Bitmap bitmap = bundle.getParcelable("data");
        if(null == bitmap)
        {
            Log.e(TAG, ">>>Bitmap为Null");
            return;
        }
        _imageView.setImageBitmap(bitmap);
    }

    /**
     * 更新完成,用于释放控件
     */
    private void IsUpdateEnd()
    {
        _progressBar.setVisibility(View.INVISIBLE);
        _edtUserRealName.setEnabled(true);
        _edtUserPhone.setEnabled(true);
        _edtPwd.setEnabled(true);
        _edtRePwd.setEnabled(true);
        _btnUpdate.setEnabled(true);
        _btnLogout.setEnabled(true);
    }

    /**
     * 正在更新,用于禁止控件
     */
    private void IsUpdateing()
    {
        _progressBar.setVisibility(View.VISIBLE);
        _edtUserRealName.setEnabled(false);
        _edtUserPhone.setEnabled(false);
        _edtPwd.setEnabled(false);
        _edtRePwd.setEnabled(false);
        _btnUpdate.setEnabled(false);
        _btnLogout.setEnabled(false);
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
            Log.e(TAG, ">>>Uri为Null");
            return;
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        //设置裁剪
        intent.putExtra("crop", "true");
        //aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        //outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 80);
        intent.putExtra("outputY", 80);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, CROP_SMALL_PICTURE);
    }

    private void ShowChoosePhotoDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(_context);
        builder.setTitle("设置头像");
        String[] items = {
                "选择本地照片",
                "拍照"
        };
        builder.setNegativeButton("取消", null);
        builder.setItems(items, (dialog, which) ->
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
                    _imageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "blowdown_userimage.jpeg"));
                    startActivityForResult(intent, TAKE_PICTURE);
                    break;
                }
                default:
                    break;
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
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
            for(String perm : permissions)
            {
                if(PackageManager.PERMISSION_GRANTED != _context.checkSelfPermission(perm))
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
            IsUpdateEnd();
            switch(message.what)
            {
                case MESSAGE_RREQUEST_FAIL:
                {
                    String result = (String) message.obj;
                    Toast.makeText(_context, "用户信息更新超时,请检查网络环境", Toast.LENGTH_SHORT).show();
                    break;
                }
                case MESSAGE_RESPONSE_SUCCESS:
                {
                    String result = (String) message.obj;
                    UserInfo userInfo = JSON.parseObject(result, UserInfo.class);
                    if(null != userInfo)
                    {
                        Log.i(TAG, ">>>用户信息更新成功");
                        UserSharedPreference.UpdateSuccess(_context, userInfo);
                        //修改用户头像
                        String imageStr = UserSharedPreference.GetUserImage(_context);
                        if(null != imageStr && !"".equals(imageStr))
                        {
                            byte[] bytes = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap bitmap = ImageUtil.ByteArrayToBitmap(bytes);
                            _imageView.setImageBitmap(bitmap);
                        }
                    }
                    else
                    {
                        Log.e(TAG, ">>>不存在该用户信息,更新失败");
                    }
                    break;
                }
                case MESSAGE_RESPONSE_FAIL:
                {
                    String result = (String) message.obj;
                    Log.e(TAG, ">>>请求超时:" + result);
                    Toast.makeText(_context, "用户信息失败,请与管理员联系", Toast.LENGTH_SHORT).show();
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
            UserInfo userInfo = new UserInfo();
            userInfo.setId(UserSharedPreference.GetUserId(_context));
            _imageView.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(_imageView.getDrawingCache());
            _imageView.setDrawingCacheEnabled(false);
            if(null != bitmap)
            {
                byte[] bytes = ImageUtil.BitmapToByteArray(bitmap);
                String imageStr = Base64.encodeToString(bytes, Base64.DEFAULT);
                userInfo.setUserImage(imageStr);
            }
            if(!"".equals(_edtRePwd.getText().toString()))
            {
                userInfo.setPassword(_edtRePwd.getText().toString());
            }
            String jsonData = JSON.toJSONString(userInfo);
            //实例化并设置连接超时时间、读取超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
            RequestBody requestBody = FormBody.create(jsonData, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder().post(requestBody).url(URL).build();
            Call call = okHttpClient.newCall(request);
            //异步请求
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e)
                {
                    Log.e(TAG, "查询用户信息失败:" + e.toString());
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
                        Log.i(TAG, "查询用户信息成功:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_SUCCESS, result);
                        handler.sendMessage(message);
                    }
                    else
                    {
                        Log.e(TAG, "查询用户信息失败:" + result);
                        Message message = handler.obtainMessage(MESSAGE_RESPONSE_FAIL, result);
                        handler.sendMessage(message);
                    }
                }
            });
            Looper.loop();
        }).start();
    }

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
                CropImage(_imageUri);
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
            _param1 = getArguments().getString(ARG_PARAM1);
            _param2 = getArguments().getString(ARG_PARAM2);
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
        _userInfoView = inflater.inflate(R.layout.fragment_user_info, container, false);
        _context = _userInfoView.getContext();
        URL = PropertiesUtil.GetValueProperties(_context).getProperty("URL") + "/UserInfo/Update";
        _edtUserName = _userInfoView.findViewById(R.id.editText_userName_userInfo);
        _edtUserRealName = _userInfoView.findViewById(R.id.editText_userRealName_userInfo);
        _edtUserRealName.setOnFocusChangeListener(this);
        _edtUserPhone = _userInfoView.findViewById(R.id.editText_userPhone_userInfo);
        _edtUserPhone.setOnFocusChangeListener(this);
        _edtPwd = _userInfoView.findViewById(R.id.editText_password_userInfo);
        _edtPwd.setOnFocusChangeListener(this);
        _edtRePwd = _userInfoView.findViewById(R.id.editText_rePassword_userInfo);
        _edtRePwd.setOnFocusChangeListener(this);
        _btnUpdate = _userInfoView.findViewById(R.id.btnUpdate_userInfo);
        _btnUpdate.setOnClickListener(this);
        _btnLogout = _userInfoView.findViewById(R.id.btnLogout_userInfo);
        _btnLogout.setOnClickListener(this);
        _imageView = _userInfoView.findViewById(R.id.imageView);
        _imageView.setOnClickListener(this);
        _progressBar = _userInfoView.findViewById(R.id.progressBar_updateUserInfo);
        //动态获取权限
        RequestPermission();
        //显示用户基本信息
        String imageStr = UserSharedPreference.GetUserImage(_context);
        if(null != imageStr && !"".equals(imageStr))
        {
            byte[] bytes = Base64.decode(imageStr, Base64.DEFAULT);
            Bitmap bitmap = ImageUtil.ByteArrayToBitmap(bytes);
            _imageView.setImageBitmap(bitmap);
        }
        _edtUserName.setText(UserSharedPreference.GetUserName(_context));
        _edtUserRealName.setText(UserSharedPreference.GetRealName(_context));
        _edtUserPhone.setText(UserSharedPreference.GetPhone(_context));
        return _userInfoView;
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

    /**
     * 该Fragment从Activity删除/替换时回调该方法,onDestroy()执行后一定会执行该方法,且只调用一次
     */
    @Override
    public void onDetach()
    {
        super.onDetach();
        _listener = null;
    }

    @Override
    public void onClick(View v)
    {
        switch(v.getId())
        {
            //选择图片
            case R.id.imageView:
                ShowChoosePhotoDialog();
                break;
            //更新信息
            case R.id.btnUpdate_userInfo:
                IsUpdateing();
                SendWithOkHttp();
                break;
            //退出登录
            case R.id.btnLogout_userInfo:
                UserSharedPreference.LogoutSuccess(_context);
                UserFragment_Login userFragment_login = new UserFragment_Login();
                getFragmentManager().beginTransaction().replace(R.id.fragment_current_user, userFragment_login, "userFragment_login").commitNow();
                break;
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        switch(v.getId())
        {
            case R.id.editText_userRealName_userInfo:
                if(hasFocus)
                {
                    _edtUserRealName.setError(null);
                }
                break;
            case R.id.editText_userPhone_userInfo:
                if(hasFocus)
                {
                }
                break;
            case R.id.editText_password_userInfo:
                if(hasFocus)
                {
                }
                break;
            case R.id.editText_rePassword_userInfo:
                if(hasFocus)
                {
                }
                break;
        }
    }

}
