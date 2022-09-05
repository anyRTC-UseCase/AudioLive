package org.ar.audiolive.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.ar.audiolive.R;

import java.util.Timer;
import java.util.TimerTask;

public class ApplyDialog extends Dialog implements View.OnClickListener {

    public interface ConfirmCallBack{
        void onClick();
    }
    public interface CancelCallBack{
        void onClick();
    }
    private ConfirmCallBack confirmCallBack;
    private CancelCallBack cancelCallBack;
    private TextView tvTitle,tvContent,tvCancel,tvConfirm;
    private String title,content,cancel,confirm;
    private Context context;
    private Timer timer;
    private int time=15;

    public ApplyDialog(@NonNull Context context,String title,String content, String cancel, String confirm, CancelCallBack cancelCallBack, ConfirmCallBack confirmCallBack) {
        super(context);
        this.confirm =confirm;
        this.cancel=cancel;
        this.title=title;
        this.content=content;
        this.context =context;
        this.confirmCallBack=confirmCallBack;
        this.cancelCallBack=cancelCallBack;
        timer =new Timer();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_apply);
        tvTitle = findViewById(R.id.tip_title);
        tvContent = findViewById(R.id.tip_content);
        tvCancel = findViewById(R.id.tip_cancel);
        tvConfirm = findViewById(R.id.tip_confirm);

        tvTitle.setText(title);
        tvContent.setText(content);
        tvCancel.setText(cancel);
        tvConfirm.setText(context.getString(R.string.apply_confirm,confirm,String.valueOf(time)));

        tvCancel.setOnClickListener(this);
        tvConfirm.setOnClickListener(this);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                new Thread(() -> {
                    time--;
                    handler.sendEmptyMessage(0);
                    if (time==0){
                        timer.cancel();
                        dismiss();
                        cancelCallBack.onClick();
                    }
                }).start();
            }
        };
        timer.schedule(timerTask, time, 1000);
    }

    private final Handler handler =new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    tvConfirm.setText(context.getString(R.string.apply_confirm,confirm,String.valueOf(time)));
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    @Override
    public void dismiss() {
        if (timer!=null){
            timer.cancel();
        }
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tip_cancel:
                if (cancelCallBack !=null){
                    cancelCallBack.onClick();
                }
                dismiss();
                break;
            case R.id.tip_confirm:
                if (confirmCallBack !=null){
                    confirmCallBack.onClick();
                }
                dismiss();
                break;
            default:
                break;
        }
    }
}
