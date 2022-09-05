package org.ar.audiolive.activity;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.ar.audiolive.R;
import org.ar.audiolive.adapter.MainPagerAdapter;
import org.ar.audiolive.dialog.LoadingDialog;
import org.ar.audiolive.fragment.InfoFragment;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.manager.RtcManager;
import org.ar.audiolive.manager.RtmManager;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;
import org.ar.audiolive.view.TabView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final String TAG =MainActivity.class.getSimpleName();
    public static final int INDEX_CONN_MIC =0;
    public static final int INDEX_INFO =1;

    private ViewPager mViewPager;
    private TabView mConnMic;
    private TabView mInfo;
    private MainPagerAdapter mainPagerAdapter;

    private List<TabView> mTabViews;
    private LoadingDialog loadingDialog;
    private String mUid;
    private ARServerManager serverManager;
    private String[] musicPath ={
            "awkward.wav",
            "chipmunk.wav",
            "guzhang.wav",
            "qihong.wav",
            "wodema.wav",
            "wuya.wav"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        registerReceiver(mBroadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (TextUtils.isEmpty(mUid)){
            serverManager.signUp();
        }else {
            serverManager.signIn(mUid);
        }
        setListener();
        try {
            writeSD(musicPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initView() {
        mViewPager =findViewById(R.id.mic_viewpager);
        mConnMic =findViewById(R.id.conn_mic);
        mInfo =findViewById(R.id.info);
        serverManager =ARServerManager.getInstance();
        loadingDialog =new LoadingDialog(this);
        loadingDialog.show();
        mUid =SpUtil.getString(Constants.UID);
        mConnMic.setOnClickListener(this);
        mInfo.setOnClickListener(this);
        mTabViews = new ArrayList<>();
        mTabViews.add(mConnMic);
        mTabViews.add(mInfo);
        if (!AndPermission.hasPermissions(this, Permission.Group.STORAGE,
                Permission.Group.MICROPHONE)){
            AndPermission.with(this).runtime().permission(
                    Permission.Group.STORAGE,
                    Permission.Group.MICROPHONE,
                    Permission.Group.CAMERA
            ).onGranted(permission->{
            }).start();
        }

        mViewPager.setOffscreenPageLimit(3);
        mainPagerAdapter= new MainPagerAdapter(getSupportFragmentManager(),this);
        mViewPager.setAdapter(mainPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            /**
             * @param position 滑动的时候,position总是代表左边的View,position+1代表右边的View
             * @param positionOffset 左边View位移的比例
             * @param positionOffsetPixels 左边的位移的像素
             */
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                mTabViews.get(position).setXPercentage(1-positionOffset);
                if (positionOffset>0){
                    mTabViews.get(position+1).setXPercentage(positionOffset);
                }
            }
        });
    }

    private void setListener(){
        serverManager.setLoginListener(signInBean -> {
            SpUtil.putString("userToken",signInBean.getData().getUserToken());
            ((InfoFragment) mainPagerAdapter.getItem(INDEX_INFO)).setAvatar(signInBean);
            loadingDialog.clearAnimation();
            RtmManager.getInstance(this).init(signInBean.getData().getAppid());
            RtcManager.getInstance(this).init(signInBean.getData().getAppid());
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateCurrentTab(int index){
        for (int i = 0; i <mTabViews.size() ; i++) {
            if (index ==i){
                mTabViews.get(i).setXPercentage(1);
            }else {
                mTabViews.get(i).setXPercentage(0);
            }
        }
    }

    /**
     * 把Assets目录下的音乐文件写入SD卡
     * @throws IOException
     */
    public void writeSD(String[] paths) throws IOException {
        AssetManager am =getAssets();
        for (int i=0;i<paths.length;i++){
            InputStream is = am.open(paths[i]);
            // 获取SD卡根路径
            Log.i("TAG", "musicWriteSD: i ="+i+",paths[i] ="+paths[i]);
            String sdPath=getExternalFilesDir(paths[i]).getPath();
            Log.i("TAG", "musicWriteSD: sdp ="+sdPath);
            FileOutputStream fos = new FileOutputStream(sdPath + "/"+paths[i]);
            // 写入SD卡
            byte[] buff = new byte[1024];
            int count = is.read(buff);
            while (count != -1){
                fos.write(buff);
                count = is.read(buff);
            }
            fos.flush();
            is.close();
            fos.close();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.conn_mic:
                mViewPager.setCurrentItem(INDEX_CONN_MIC,false);
                updateCurrentTab(INDEX_CONN_MIC);
                break;
            case R.id.info:
                mViewPager.setCurrentItem(INDEX_INFO,false);
                updateCurrentTab(INDEX_INFO);
                break;
        }
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {

            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            Bundle bundle = intent.getExtras();
            NetworkInfo aNetworkInfo = (NetworkInfo) bundle.get(ConnectivityManager.EXTRA_NETWORK_INFO);

            if (aNetworkInfo != null) {
                if (aNetworkInfo.isConnected()) {
                    //表示网络已连接
                    if (!TextUtils.isEmpty(mUid)){
                        serverManager.signIn(mUid);
                    }
                } else if (!wifi.isConnected() && !mobile.isConnected()) {
                    //表示wifi和数据连接都断开了，可以弹出一个toast等
                    //Toast.makeText(MainActivity.this, "网络异常，检查网络重新再试", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }
}