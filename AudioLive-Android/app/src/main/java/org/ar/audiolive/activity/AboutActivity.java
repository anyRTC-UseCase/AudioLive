package org.ar.audiolive.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.ar.audiolive.R;
import org.ar.rtc.RtcEngine;

public class AboutActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =AboutActivity.class.getSimpleName();

    private RelativeLayout rlPrivacyRegulations,rlStatement,rlRegisterAccount;
    private ImageView back;
    private TextView mTitle,mSave,mTimes,mSdkVersion,mSoftwareVersion;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        rlPrivacyRegulations =findViewById(R.id.rl_privacy_regulations);
        rlStatement =findViewById(R.id.rl_ar_statement);
        rlRegisterAccount =findViewById(R.id.rl_register_account);
        back =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);
        mSave =findViewById(R.id.save);
        mTimes =findViewById(R.id.times);
        mSdkVersion =findViewById(R.id.version_text);
        mSoftwareVersion =findViewById(R.id.software_version_text);

        mTitle.setText("关于");
        mSoftwareVersion.setText("V "+getSoftwareVersion());
        mSdkVersion.setText("V "+ RtcEngine.getSdkVersion());
        try {
            ApplicationInfo info =getPackageManager().getApplicationInfo(getPackageName(),PackageManager.GET_META_DATA);
            String releaseTime =info.metaData.getString("releaseTime");
            mTimes.setText(releaseTime);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        mSave.setVisibility(View.GONE);
        back.setOnClickListener(this);
        rlPrivacyRegulations.setOnClickListener(this);
        rlStatement.setOnClickListener(this);
        rlRegisterAccount.setOnClickListener(this);
    }

    private String getSoftwareVersion(){
        try {
            return getPackageManager().getPackageInfo(getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Intent intent =null;
        switch (view.getId()){
            case R.id.rl_ar_statement:
                intent =new Intent(this, SettingNameActivity.class);
                intent.putExtra("type",SettingNameActivity.AR_STATEMENT_TYPE);
                startActivity(intent);
                break;
            case R.id.rl_privacy_regulations:
                String link = getString(R.string.privacy_website_link);
                Uri uri = Uri.parse(link);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.rl_register_account:
                link = getString(R.string.register_website_link);
                uri = Uri.parse(link);
                intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }


}
