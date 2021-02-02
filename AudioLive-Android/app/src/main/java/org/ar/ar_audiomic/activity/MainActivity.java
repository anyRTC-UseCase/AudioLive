package org.ar.ar_audiomic.activity;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bumptech.glide.Glide;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.ar.ar_audiomic.ARApplication;
import org.ar.ar_audiomic.R;
import org.ar.ar_audiomic.adapter.MainPagerAdapter;
import org.ar.ar_audiomic.bean.SignInBean;
import org.ar.ar_audiomic.dialog.LoadingDialog;
import org.ar.ar_audiomic.fragment.InfoFragment;
import org.ar.ar_audiomic.manager.ARServerManager;
import org.ar.ar_audiomic.manager.RtcManager;
import org.ar.ar_audiomic.manager.RtmManager;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;
import org.ar.ar_audiomic.view.TabView;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (TextUtils.isEmpty(mUid)){
            serverManager.signUp();
        }else {
            serverManager.signIn(mUid);
        }
        setListener();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}