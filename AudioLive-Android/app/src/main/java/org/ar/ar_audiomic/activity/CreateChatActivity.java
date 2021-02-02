package org.ar.ar_audiomic.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.yanzhenjie.kalle.JsonBody;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.ar.ar_audiomic.R;
import org.ar.ar_audiomic.bean.AddBean;
import org.ar.ar_audiomic.bean.SignInBean;
import org.ar.ar_audiomic.manager.ARServerManager;
import org.ar.ar_audiomic.manager.ChatRoomManager;
import org.ar.ar_audiomic.manager.RtmManager;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;
import org.ar.rtm.ErrorInfo;
import org.ar.rtm.ResultCallback;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =CreateChatActivity.class.getSimpleName();

    private ImageView back;
    private TextView mTitle;
    private EditText mEtChat;
    private Button mConfirm;
    private ARServerManager serverManager;
    private ChatRoomManager chatRoomManager;

    private TextWatcher textWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length()>0){
                mConfirm.setSelected(true);
                mConfirm.setEnabled(true);
            }else {
                mConfirm.setSelected(false);
                mConfirm.setEnabled(false);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        serverManager =ARServerManager.getInstance();
        chatRoomManager =ChatRoomManager.instance(this);
        back =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);
        mEtChat =findViewById(R.id.et_create_chat);
        mConfirm =findViewById(R.id.btn_create);
        init();
    }

    private void init() {
        mTitle.setText("创建语音房间");
        mEtChat.addTextChangedListener(textWatcher);
        if (mEtChat.getEditableText().length()>0){
            mConfirm.setSelected(true);
            mConfirm.setEnabled(true);
        }else {
            mConfirm.setSelected(false);
            mConfirm.setEnabled(false);
        }
        back.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
        setListener();
    }

    private void setListener(){
        serverManager.setAddRoomListener(addBean -> {
            chatRoomManager.login(addBean.getData().getRtmToken(),SpUtil.getString(Constants.UID));
            chatRoomManager.joinRtcChannel(addBean.getData().getRtcToken(),addBean.getData().getRoomId());
            Intent intent =new Intent(CreateChatActivity.this,ChatActivity.class);
            intent.putExtra(Constants.INTENT_PUSH_URL,addBean.getData().getPushUrl());
            intent.putExtra(Constants.INTENT_ROOM_ID,addBean.getData().getRoomId());
            intent.putExtra(Constants.INTENT_ROOM_NAME,mEtChat.getText().toString());
            intent.putExtra(Constants.INTENT_IS_ANCHOR,true);
            startActivity(intent);
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_create:
                serverManager.addRoom(mEtChat.getText().toString());
                finish();
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }
}
