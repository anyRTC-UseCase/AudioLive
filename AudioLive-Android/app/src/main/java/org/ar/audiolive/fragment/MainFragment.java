package org.ar.audiolive.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


import org.ar.audiolive.R;
import org.ar.audiolive.activity.ChatActivity;
import org.ar.audiolive.activity.CreateChatActivity;
import org.ar.audiolive.adapter.RoomListAdapter;
import org.ar.audiolive.bean.JoinRoomBean;
import org.ar.audiolive.bean.RoomBean;
import org.ar.audiolive.bean.RoomListBean;
import org.ar.audiolive.manager.ARServerManager;
import org.ar.audiolive.manager.ChatRoomManager;
import org.ar.audiolive.util.Constants;
import org.ar.audiolive.util.SpUtil;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG =MainFragment.class.getSimpleName();

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private Button notRoom;
    private RoomListAdapter roomListAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ImageView imgCreate;
    private Context mContext;
    private int mCount =10;

    private ARServerManager serverManager;
    private ChatRoomManager mChatRoomManager;
    private static MainFragment mainFragment;

    public MainFragment(Context context){
        mContext =context;
    }

    public static MainFragment getInstance(Context context){
        if (mainFragment ==null){
            synchronized (MainFragment.class){
                if (mainFragment ==null){
                    mainFragment =new MainFragment(context);
                }
            }
        }
        return mainFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_conn_mic,container,false);
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serverManager =ARServerManager.getInstance();
        mChatRoomManager =ChatRoomManager.instance(mContext);
        swipeRefreshLayout =view.findViewById(R.id.mic_swipe_sr);
        recyclerView =view.findViewById(R.id.mic_recyclerview);
        notRoom =view.findViewById(R.id.not_room);
        staggeredGridLayoutManager =new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);
        roomListAdapter =new RoomListAdapter();
        recyclerView.setAdapter(roomListAdapter);
        imgCreate=view.findViewById(R.id.create_img);
        setSwipeStyle();
        imgCreate.setOnClickListener(this);
        setClickListener();
        setListener();
        notRoom.setOnTouchListener((view1, motionEvent) -> swipeRefreshLayout.dispatchTouchEvent(motionEvent));
    }

    private void setSwipeStyle() {
        swipeRefreshLayout.setColorSchemeResources(R.color.tab_color);
        swipeRefreshLayout.setDistanceToTriggerSync(100);// 设置手指在屏幕下拉多少距离会触发下拉刷新
        swipeRefreshLayout.setEnabled(true);
        swipeRefreshLayout.setOnRefreshListener(() ->{
            serverManager.getRoomList();
            handler.sendEmptyMessageDelayed(0,1000);
        });
    }

    private Handler handler =new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    mCount--;
                    handler.sendEmptyMessageDelayed(0,1000);
                    if (mCount==0){
                        handler.removeMessages(0);
                        mCount=10;
                        swipeRefreshLayout.setRefreshing(false);
                        existAvailableNetwork();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 判断是否有可用网络
     */
    public boolean existAvailableNetwork() {
        ConnectivityManager connectivity = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }else if (info[i].getState() == NetworkInfo.State.DISCONNECTED){
                        Toast.makeText(mContext, "网络异常，检查网络重新再试", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        }
        return false;
    }

    private void setClickListener(){
        roomListAdapter.setOnItemClickListener((adapter, view, position) -> {
            String roomId =roomListAdapter.getData().get(position).getRoomId();
            serverManager.joinRoom(roomId,position);
        });
    }

    private void setListener(){
        serverManager.setRoomListListener(new ARServerManager.RoomListEventListener() {
            @Override
            public void showRoomList(RoomListBean roomListBean) {
                showRoom(roomListBean,true);
            }

            @Override
            public void refreshRoomList(RoomListBean roomListBean,boolean isHasRoom) {
                showRoom(roomListBean,isHasRoom);
                handler.removeMessages(0);
                mCount=10;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void joinRoom(JoinRoomBean joinRoomBean) {
                String roomId =joinRoomBean.getData().getRoom().getRoomId();
                String roomName =joinRoomBean.getData().getRoom().getRoomName();
                String rtcToken =joinRoomBean.getData().getRoom().getRtcToken();
                int roomType =joinRoomBean.getData().getRoom().getrType();
                String rtmpUrl =joinRoomBean.getData().getPullRtmpUrl();
                SpUtil.putString(Constants.ANCHOR_UID,joinRoomBean.getData().getRoom().getOwer().getUid());
                SpUtil.putString(Constants.ANCHOR_NAME,joinRoomBean.getData().getRoom().getOwer().getUserName());
                mChatRoomManager.login(joinRoomBean.getData().getRoom().getRtmToken(),SpUtil.getString(Constants.UID));
                serverManager.getJoinUserList(roomId);
                Intent intent =new Intent(mContext, ChatActivity.class);
                intent.putExtra(Constants.INTENT_ROOM_ID,roomId);
                intent.putExtra(Constants.INTENT_ROOM_NAME,roomName);
                intent.putExtra(Constants.INTENT_RTMP_URL,rtmpUrl);
                intent.putExtra(Constants.INTENT_IS_ANCHOR,false);
                intent.putExtra(Constants.ROOM_TYPE,joinRoomBean.getData().getRoom().getrType());
                mContext.startActivity(intent);
            }
        });
    }

    private void showRoom(RoomListBean roomListBean,boolean isHasRoom) {
        String imgUrl,roomName,roomId;
        int roomNum;
        List<RoomBean> roomBeans =new ArrayList<>();
        if (isHasRoom) {
            for (int i = 0; i < roomListBean.getData().getList().size(); i++) {
                imgUrl=roomListBean.getData().getList().get(i).getImageUrl();
                roomName =roomListBean.getData().getList().get(i).getRoomName();
                roomNum =roomListBean.getData().getList().get(i).getUserNum();
                roomId =roomListBean.getData().getList().get(i).getRoomId();
                roomBeans.add(new RoomBean(imgUrl,roomName,roomNum,roomId));
            }
        }
        //Collections.reverse(roomBeans); //倒序
        roomListAdapter.setNewInstance(roomBeans);
        if (roomListAdapter.getItemCount()==0){
            notRoom.setVisibility(View.VISIBLE);
        }else {
            notRoom.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.create_img) {
            Intent intent = new Intent(mContext, CreateChatActivity.class);
            mContext.startActivity(intent);
        }
    }
}
