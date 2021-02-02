package org.ar.ar_audiomic.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

import org.ar.ar_audiomic.ARApplication;
import org.ar.ar_audiomic.R;
import org.ar.ar_audiomic.activity.AboutActivity;
import org.ar.ar_audiomic.activity.SettingNameActivity;
import org.ar.ar_audiomic.bean.SignInBean;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;

import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

public class InfoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG =InfoFragment.class.getSimpleName();

    private RelativeLayout rlName,rlAbout;
    private TextView name;
    private TextView setName;
    private Context mContext;
    private CircleImageView mAvatar;
    private String userName;
    @SuppressLint("StaticFieldLeak")
    private static InfoFragment infoFragment;

    public InfoFragment(Context context){
        mContext =context;
    }

    public static InfoFragment getInstance(Context context){
        if (infoFragment ==null){
            synchronized (InfoFragment.class){
                if (infoFragment ==null){
                    infoFragment =new InfoFragment(context);
                }
            }
        }
        return infoFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rlName =view.findViewById(R.id.rl_name);
        rlAbout =view.findViewById(R.id.rl_about);
        name =view.findViewById(R.id.info_name);
        setName =view.findViewById(R.id.name);
        mAvatar =view.findViewById(R.id.my_avatar);
        rlName.setOnClickListener(this);
        rlAbout.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        userName =SpUtil.getString(Constants.USER_NAME);
        if (!TextUtils.isEmpty(userName)){
            setName.setText(userName);
            name.setText(userName);
        }else {
            setName.setText(Build.MODEL);
            name.setText(Build.MODEL);
        }
    }

    public void setAvatar(SignInBean signInBean){
        if (signInBean !=null){
            if (Glide.with(ARApplication.the().getContent()).isPaused()){
                Glide.with(ARApplication.the().getContent()).onStart();
            }
            Glide.with(ARApplication.the().getContent())
                    .load(signInBean.getData().getAvatar())
                    .error(R.drawable.ic_unkown)
                    .into(mAvatar);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.rl_name:
                Intent intent =new Intent(mContext, SettingNameActivity.class);
                intent.putExtra("type",SettingNameActivity.SETTING_NAME_TYPE);
                mContext.startActivity(intent);
                break;
            case R.id.rl_about:
                Intent i =new Intent(mContext, AboutActivity.class);
                mContext.startActivity(i);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Glide.with(ARApplication.the().getContent()).pauseRequests();
    }
}
