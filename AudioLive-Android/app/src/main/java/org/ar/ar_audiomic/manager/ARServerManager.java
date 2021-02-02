package org.ar.ar_audiomic.manager;

import android.os.Build;
import android.util.Log;

import com.google.gson.Gson;
import com.yanzhenjie.kalle.JsonBody;
import com.yanzhenjie.kalle.Kalle;
import com.yanzhenjie.kalle.simple.SimpleCallback;
import com.yanzhenjie.kalle.simple.SimpleResponse;

import org.ar.ar_audiomic.ARApplication;
import org.ar.ar_audiomic.bean.AddBean;
import org.ar.ar_audiomic.bean.JoinRoomBean;
import org.ar.ar_audiomic.bean.JoinUserBean;
import org.ar.ar_audiomic.bean.MusicBean;
import org.ar.ar_audiomic.bean.RoomListBean;
import org.ar.ar_audiomic.bean.SignInBean;
import org.ar.ar_audiomic.bean.SignUpBean;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;
import org.json.JSONException;
import org.json.JSONObject;

public class ARServerManager {

    private static final String TAG = ARServerManager.class.getSimpleName();

    private static ARServerManager instance;
    private Gson gson =new Gson();

    private SignUpBean signUpBean;
    private SignInBean signInBean;
    private RoomListBean roomListBean;
    private JoinRoomBean joinRoomBean;
    private JoinUserBean joinUserBean;
    private AddBean addBean;
    private MusicBean musicBean;

    public interface LoginEventListener{
        void loginSuccess(SignInBean signInBean);
    }

    public interface RoomListEventListener{
        void showRoomList(RoomListBean roomListBean);
        void refreshRoomList(RoomListBean roomListBean,boolean isHasRoom);
        void joinRoom(JoinRoomBean joinRoomBean,String roomId,String roomName,String pullRtmpUrl);
    }

    public interface AddRoomEventListener{
        void addRoom(AddBean addBean);
    }

    public interface UpdateNameEventListener{
        void updateNameState(boolean isSuccess);
    }

    public interface UpdateUserEventListener{
        void updateUserState(JoinUserBean joinUserBean);
    }

    private LoginEventListener loginListener;
    private RoomListEventListener roomListener;
    private AddRoomEventListener addRoomListener;
    private UpdateNameEventListener updateNameListener;
    private UpdateUserEventListener updateUserListener;



    private ARServerManager(){
    }

    public static ARServerManager getInstance(){
        if (instance ==null){
            synchronized (ARServerManager.class){
                if (instance ==null){
                    instance =new ARServerManager();
                }
            }
        }
        return instance;
    }

    public JoinUserBean getJoinUserBean() {
        return joinUserBean;
    }

    public void setJoinUserBean(JoinUserBean joinUserBean) {
        this.joinUserBean = joinUserBean;
    }

    public SignUpBean getSignUpBean() {
        return signUpBean;
    }

    public SignInBean getSignInBean() {
        return signInBean;
    }

    public RoomListBean getRoomListBean() {
        return roomListBean;
    }

    public JoinRoomBean getJoinRoomBean() {
        return joinRoomBean;
    }

    public AddBean getAddBean() {
        return addBean;
    }

    public MusicBean getMusicBean() {
        return musicBean;
    }

    public void setLoginListener(LoginEventListener listener){
        loginListener =listener;
    }

    public void setRoomListListener(RoomListEventListener listener){
        roomListener =listener;
    }

    public void setAddRoomListener(AddRoomEventListener listener){
        addRoomListener =listener;
    }

    public void setUpdateNameListener(UpdateNameEventListener listener){
        updateNameListener =listener;
    }

    public void setUpdateUserListener(UpdateUserEventListener listener){
        updateUserListener =listener;
    }



    /**
     * 注册
     */
    public void signUp(){
        JSONObject params = new JSONObject();
        try {
            params.put("sex",0);
            params.put("userName", Build.MODEL);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/signUp"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse:signUp  response ="+response.succeed());
                        if (response.succeed().contains("success")){
                            signUpBean =gson.fromJson(response.succeed(),SignUpBean.class);
                            if (signUpBean !=null){
                                String uid =signUpBean.getData().getUid();
                                SpUtil.putString(Constants.UID,uid);
                                signIn(uid);
                            }
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 登录
     * @param uid 用户ID
     */
    public void signIn(String uid){
        JSONObject params = new JSONObject();
        try {
            params.put("cType",1);
            params.put("pkg",ARApplication.the().getApplicationContext().getPackageName());
            params.put("uid",uid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/signIn"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: signIn success ="+response.succeed());
                        if (response.succeed().contains("success")){
                            signInBean =gson.fromJson(response.succeed(),SignInBean.class);
                            getRoomList(signInBean);
                            if (loginListener !=null){
                                loginListener.loginSuccess(signInBean);
                            }
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                    }
                });
    }

    /**
     * 获取房间列表
     */
    public void getRoomList(SignInBean signInBean){
        JSONObject params = new JSONObject();
        try {
            params.put("count", Integer.MAX_VALUE);
            params.put("rType", Constants.R_TYPE_LIVE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/getRoomList"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: getRoomList ="+response.succeed());
                        if (response.succeed().contains("success")){
                            roomListBean =gson.fromJson(response.succeed(), RoomListBean.class);
                            if (roomListener !=null){
                                roomListener.showRoomList(roomListBean);
                            }
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: ===> ="+e.getMessage());
                    }
                });
    }

    /**
     * 刷新房间列表
     */
    public void refreshRoomList(){
        JSONObject params = new JSONObject();
        try {
            params.put("count",Integer.MAX_VALUE);
            if (roomListBean!=null){
                params.put("nextRoomId",roomListBean.getData().getNextId());
            }else {
                params.put("nextRoomId",0);
            }
            params.put("rType", Constants.R_TYPE_LIVE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/refreshRoomList"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: response="+response.succeed());
                        boolean isHasRoom =false;
                        if (response.succeed().contains("success")){
                            roomListBean =gson.fromJson(response.succeed(),RoomListBean.class);
                            isHasRoom =true;
                        }
                        if (roomListener !=null){
                            roomListener.refreshRoomList(roomListBean,isHasRoom);
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    /**
     * 获取房间人员
     * @param roomId：房间ID
     */
    public void getJoinUserList(String roomId){
        JSONObject params = new JSONObject();
        try {
            params.put("count",Integer.MAX_VALUE);
            params.put("mike",99);
            params.put("nextId",0);
            params.put("role",99);
            params.put("roomId",roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/getJoinUserList"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: getJoinUserList data ="+response.succeed());
                        if (response.succeed().contains("success")){
                            joinUserBean =gson.fromJson(response.succeed(),JoinUserBean.class);
                        }
                        if (updateUserListener !=null){
                            updateUserListener.updateUserState(joinUserBean);
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    /**
     * 加入房间
     * @param roomId
     * @param roomName
     * @param pullRtmpUrl
     */
    public void joinRoom(String roomId,String roomName,String pullRtmpUrl){
        JSONObject params = new JSONObject();
        try {
            params.put("cType",1);
            params.put("pkg",ARApplication.the().getApplicationContext().getPackageName());
            params.put("roomId",roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/joinRoom"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: joinRoom data ="+response.succeed());
                        if (response.succeed().contains("success")){
                            joinRoomBean =gson.fromJson(response.succeed(),JoinRoomBean.class);
                            if (roomListener !=null){
                                roomListener.joinRoom(joinRoomBean,roomId,roomName,pullRtmpUrl);
                            }
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    public void addMusic(int musicId,int state,String roomId){
        JSONObject params = new JSONObject();
        try {
            params.put("musicId", musicId);
            params.put("musicState",state);
            params.put("roomId",roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/addMusic"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: addMusic data ="+response.succeed());
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    /**
     * 创建房间
     * @param roomName 房间名称
     */
    public void addRoom(String roomName){
        JSONObject params = new JSONObject();
        try {
            params.put("cType", 1);
            params.put("pkg", ARApplication.the().getApplicationContext().getPackageName());
            params.put("rType", 1);
            params.put("roomName",roomName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/addRoom"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: ="+response.succeed());
                        if (response.succeed().contains("success")){
                            addBean =gson.fromJson(response.succeed(),AddBean.class);
                            if (addRoomListener !=null){
                                addRoomListener.addRoom(addBean);
                            }
                        }

                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    /**
     * 更新用户名
     * @param name 用户名
     */
    public void updateUserName(String name){
        JSONObject params = new JSONObject();
        try {
            params.put("userName", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/updateUserName"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: updateUserName ="+response.succeed());
                        SpUtil.putString(Constants.USER_NAME,name);
                        if (updateNameListener !=null){
                            updateNameListener.updateNameState(true);
                        }
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                        if (updateNameListener !=null){
                            updateNameListener.updateNameState(false);
                        }
                    }
                });
    }


    /**
     * 获取音乐列表
     */
    public void getMusicList(){
        Kalle.get(String.format(Constants.SERVER,"user/getMusicList"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+ signInBean.getData().getUserToken())
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: getMusicList ="+response.succeed());
                        musicBean = gson.fromJson(response.succeed(),MusicBean.class);
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                    }
                });
    }

    /**
     * 删除房间
     * @param roomId 房间ID
     */
    public void deleteRoom(String roomId){
        JSONObject params = new JSONObject();
        try {
            params.put("roomId", roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/deleteRoom"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "deleteRoom onResponse: ="+response.succeed());
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }

    /**
     * 主播踢人
     * @param userId 被踢者
     * @param roomId 房间ID
     */
    public void updateUserLeaveTs(String userId,String roomId){
        JSONObject params = new JSONObject();
        try {
            params.put("uid", userId);
            params.put("roomId", roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/updateUserLeaveTs"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: ="+response.succeed());
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }


    /**
     * 用户离开房间
     * @param roomId 房间ID
     */
    public void updateV2UserLeaveTs(String roomId){
        JSONObject params = new JSONObject();
        try {
            params.put("roomId", roomId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Kalle.post(String.format(Constants.SERVER,"user/updateV2UserLeaveTs"))
                .addHeader(Constants.CONTENT_TYPE,"application/json")
                .addHeader(Constants.AUTHORIZATION,"Bearer "+signInBean.getData().getUserToken())
                .body(new JsonBody(params.toString()))
                .perform(new SimpleCallback<String>() {
                    @Override
                    public void onResponse(SimpleResponse<String, String> response) {
                        Log.i(TAG, "onResponse: ="+response.succeed());
                    }

                    @Override
                    public void onException(Exception e) {
                        super.onException(e);
                        e.printStackTrace();
                        Log.i(TAG, "onException: e="+e.getMessage());
                    }
                });
    }
}
