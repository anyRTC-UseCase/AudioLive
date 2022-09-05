package org.ar.audiolive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import org.ar.audiolive.R;

public class LoadingDialog extends Dialog {

    private ImageView ivLoading;
    private Animation animation;
    private Context context;

    public LoadingDialog(@NonNull Context context) {
        super(context);
        this.context =context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        ivLoading =findViewById(R.id.loading);
        animation = AnimationUtils.loadAnimation(context,R.anim.animation_rotate);
        ivLoading.startAnimation(animation);
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(params);
        }
    }


    public void clearAnimation(){
        ivLoading.clearAnimation();
        dismiss();
    }


}
