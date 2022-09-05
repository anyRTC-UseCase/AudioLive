package org.ar.audiolive.activity;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.ar.audiolive.R;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;

public class SettingNameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =SettingNameActivity.class.getSimpleName();
    public static final int SETTING_NAME_TYPE =0;
    public static final int AR_STATEMENT_TYPE =1;

    private static final int ET_ENTRY_MAX =16;
    private int type;

    private RelativeLayout rlSettingName;
    private EditText etName;
    private ImageView imgClear,mBack;
    private TextView mStatement,mCount,mTitle,mSave;
    private ARServerManager serverManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_name);
        type =getIntent().getIntExtra("type",SETTING_NAME_TYPE);
        rlSettingName =findViewById(R.id.rl_setting_name);
        etName =findViewById(R.id.entry_name);
        imgClear =findViewById(R.id.entry_clear);
        mStatement =findViewById(R.id.text_ar_statement);
        mCount =findViewById(R.id.name_count);
        mBack =findViewById(R.id.back);
        mTitle =findViewById(R.id.title_content);
        mSave =findViewById(R.id.save);
        serverManager =ARServerManager.getInstance();
        if (type ==SETTING_NAME_TYPE){
            showSettingNameUI();
        }else {
            showStatement();
        }
        mBack.setOnClickListener(this);
        setListener();

    }

    private void setListener(){
        serverManager.setUpdateNameListener(new ARServerManager.UpdateNameEventListener() {
            @Override
            public void updateNameState(boolean isSuccess) {
                if (isSuccess){
                    Toast.makeText(SettingNameActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                    finish();
                }else {
                    Toast.makeText(SettingNameActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showSettingNameUI() {
        mTitle.setText("设置昵称");
        rlSettingName.setVisibility(View.VISIBLE);
        mStatement.setVisibility(View.GONE);
        mSave.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(SpUtil.getString(Constants.USER_NAME))){
            etName.setText(SpUtil.getString(Constants.USER_NAME));
        }else {
            etName.setText(Build.MODEL);
        }
        etName.addTextChangedListener(textWatcher);
        etName.clearComposingText();
        etName.setSelection(etName.getEditableText().length());
        if (etName.getEditableText().length() >0){
            imgClear.setVisibility(View.VISIBLE);
            mSave.setSelected(true);
            mSave.setClickable(true);
            mCount.setText(String.valueOf(ET_ENTRY_MAX-etName.getEditableText().length()));
        }else{
            imgClear.setVisibility(View.GONE);
            mSave.setSelected(false);
            mSave.setClickable(false);
            mCount.setText(String.valueOf(ET_ENTRY_MAX));
        }


        mSave.setOnClickListener(this);
        imgClear.setOnClickListener(this);
        Log.i(TAG, "showSettingNameUI: ="+etName.getEditableText().length());
    }

    private void showStatement() {
        mTitle.setText("免责声明");
        rlSettingName.setVisibility(View.GONE);
        mSave.setVisibility(View.GONE);
        mStatement.setVisibility(View.VISIBLE);
    }

    private TextWatcher textWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.i(TAG, "beforeTextChanged: --->");
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            Log.i(TAG, "onTextChanged: --->");
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable.length() > 0){
                imgClear.setVisibility(View.VISIBLE);
                mSave.setSelected(true);
                mSave.setClickable(true);
                mCount.setText(String.valueOf(ET_ENTRY_MAX-editable.length()));
            } else{
                imgClear.setVisibility(View.GONE);
                mSave.setSelected(false);
                mSave.setClickable(false);
                mCount.setText(String.valueOf(ET_ENTRY_MAX));
            }
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.back:
                finish();
                break;
            case R.id.entry_clear:
                etName.setText("");
                break;
            case R.id.save:
                serverManager.updateUserName(etName.getText().toString());
                break;
            default:
                break;
        }
    }
}
