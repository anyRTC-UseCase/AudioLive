package org.ar.audiolive.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ar.audiolive.R;
import org.ar.audiolive.adapter.WaitMicAdapter;
import org.ar.audiolive.manager.ChatRoomManager;
import org.ar.audiolive.model.ChannelData;
import org.ar.audiolive.model.WaitMicBean;
import org.ar.audiolive.util.Constants;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WaitMicDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = WaitMicDialog.class.getSimpleName();
    private ImageView mClose;
    private RecyclerView mWaitRecycler;
    private Button mBtnQuickMic;
    private TextView tvWaitNone;
    private final Context context;
    private List<String> mWaitingList;
    private ChatRoomManager chatRoomManager;
    private ChannelData channelData;
    private WaitMicAdapter waitMicAdapter;
    private final ClickCallBack clickCallBack;
    public interface ClickCallBack{
        void onClick(String userId);
    }

    public WaitMicDialog(@NonNull Context context,List<String> waitingList,ClickCallBack callBack) {
        super(context,R.style.dialog);
        this.context =context;
        this.mWaitingList =waitingList;
        this.clickCallBack =callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_wait_mic);
        mClose =findViewById(R.id.wait_mic_close);
        mWaitRecycler =findViewById(R.id.wait_mic_recycler);
        mBtnQuickMic =findViewById(R.id.quick_mic);
        tvWaitNone =findViewById(R.id.wait_mic_none);
        chatRoomManager=ChatRoomManager.instance(context);
        channelData =chatRoomManager.getChannelData();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        mWaitRecycler.setLayoutManager(linearLayoutManager);
        waitMicAdapter =new WaitMicAdapter(context);
        mWaitRecycler.setAdapter(waitMicAdapter);
        mClose.setOnClickListener(this);
        mBtnQuickMic.setOnClickListener(this);
        addItem();
        setListener();
    }

    public void setDataList(List<String> waitingList){
        this.mWaitingList =waitingList;
        Log.i(TAG, "setDataList: mWaitingList size  ="+mWaitingList.size());
        addItem();
    }

    @SuppressLint("NonConstantResourceId")
    private void setListener() {
        waitMicAdapter.addChildClickViewIds(R.id.wait_mic_agree,R.id.wait_mic_refuse);
        waitMicAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            String userId =waitMicAdapter.getItem(position).getUserId();
            JSONObject jsonObject =new JSONObject();
            switch (view.getId()){
                case R.id.wait_mic_refuse:
                    try {
                        jsonObject.put(Constants.CMD,Constants.MIC_REJECT_LINE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    chatRoomManager.getRtmManager().sendMessageToPeer(userId,jsonObject.toString(),null);
                    clickCallBack.onClick(userId);
                    break;
                case R.id.wait_mic_agree:
                    try {
                        jsonObject.put(Constants.CMD, Constants.MIC_ACCEPT_LINE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    chatRoomManager.getRtmManager().sendMessageToPeer(userId,jsonObject.toString(),null);
                    clickCallBack.onClick(userId);
                    break;
                default:
                    break;
            }
            waitMicAdapter.remove(position);
            if (waitMicAdapter.getItemCount() ==0){
                tvWaitNone.setVisibility(View.VISIBLE);
                mWaitRecycler.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.BOTTOM;
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
    }

    private void addItem(){
        if (mWaitingList.size()!=0){
            tvWaitNone.setVisibility(View.GONE);
            mWaitRecycler.setVisibility(View.VISIBLE);
            List<WaitMicBean> waitMicBeanList =new ArrayList<>();
            Log.i(TAG, "addItem: mWaitingList.size ="+mWaitingList.size());
            for (int i = 0; i <mWaitingList.size() ; i++) {
                String name =channelData.getName(mWaitingList.get(i));
                String avatar =channelData.getAvatar(mWaitingList.get(i));
                waitMicBeanList.add(new WaitMicBean(mWaitingList.get(i),avatar,name));
                waitMicAdapter.setNewInstance(waitMicBeanList);
            }
        }else {
            tvWaitNone.setVisibility(View.VISIBLE);
            mWaitRecycler.setVisibility(View.GONE);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.wait_mic_close:
                dismiss();
                break;
            case R.id.quick_mic:
                setQuickMic();
                break;
            default:
                break;
        }
    }

    private void setQuickMic() {
        if (waitMicAdapter.getItemCount()>0){
            JSONObject jsonObject =new JSONObject();
            int size =0;
            for (int i = 0; i <waitMicAdapter.getItemCount() ; i++) {
                String userId =waitMicAdapter.getItem(i).getUserId();
                try {
                    jsonObject.put(Constants.CMD,Constants.MIC_ACCEPT_LINE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                chatRoomManager.getRtmManager().sendMessageToPeer(userId,jsonObject.toString(),null);
                clickCallBack.onClick(userId);
                size++;
            }
            Log.i(TAG, "setQuickMic: size ="+size);
            for (int i = 0; i <size; i++) {
                Log.i(TAG, "setQuickMic: pos ="+i);
                waitMicAdapter.remove(0);
            }
            if (waitMicAdapter.getItemCount() ==0){
                tvWaitNone.setVisibility(View.VISIBLE);
                mWaitRecycler.setVisibility(View.GONE);
            }
        }
    }
}
