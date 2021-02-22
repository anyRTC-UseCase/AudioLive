package org.ar.ar_audiomic.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.ar.ar_audiomic.R;
import org.ar.ar_audiomic.util.StatusBarUtil;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtil.setDeepStatusBar(true, LaunchActivity.this, Color.TRANSPARENT);
        StatusBarUtil.setStatusBarColor(LaunchActivity.this, R.color.launch_color);
        setContentView(R.layout.activity_launch);
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Intent intent =new Intent(LaunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }).start();
    }
}
