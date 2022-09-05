package org.ar.audiolive.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.kongzue.dialog.v3.BottomMenu;

import org.ar.audiolive.R;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.manager.ChatRoomManager;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;

public class CreateChatActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =CreateChatActivity.class.getSimpleName();

    private ImageView back;
    private TextView mTitle;
    private EditText mEtChat;
    private Button mConfirm;
    private ARServerManager serverManager;
    private ChatRoomManager chatRoomManager;
    private int mRoomType;

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
            chatRoomManager.joinRtcChannel(addBean.getData().getRtcToken(),addBean.getData().getRoomId(),mRoomType,true);
            Intent intent =new Intent(CreateChatActivity.this,ChatActivity.class);
            intent.putExtra(Constants.INTENT_RTMP_URL,addBean.getData().getPushUrl());
            intent.putExtra(Constants.INTENT_ROOM_ID,addBean.getData().getRoomId());
            intent.putExtra(Constants.INTENT_ROOM_NAME,mEtChat.getText().toString());
            intent.putExtra(Constants.INTENT_IS_ANCHOR,true);
            intent.putExtra(Constants.ROOM_TYPE,mRoomType);
            startActivity(intent);
        });
    }

    private void showScheme(){
        BottomMenu.show(this, "方案选择", new String[]{"RTC 实时直播", "客户端推流到 CDN","服务端推流到 CDN"}, (text, index) -> {
            mRoomType =index+1;
            serverManager.addRoom(mEtChat.getText().toString(),mRoomType);
            finish();
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_create:
                showScheme();
                break;
            case R.id.back:
                finish();
                break;
            default:
                break;
        }
    }
}
