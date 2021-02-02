package org.ar.ar_audiomic.model;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.ar.ar_audiomic.manager.ChatRoomManager;
import org.ar.ar_audiomic.util.Constants;
import org.ar.ar_audiomic.util.SpUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChannelData  {

    private String anchorId;
    private String userName;
    private List<Member> mMemberList =new ArrayList<>();

    public String getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(String anchorId) {
        this.anchorId = anchorId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public List<Member> getMemberList() {
        return mMemberList;
    }

    public void setMemberList(List<Member> mMemberList) {
        this.mMemberList = mMemberList;
    }

    public void addOrUpdateMember(Member member){
        int index = -1;
        for (int i = 0; i <mMemberList.size() ; i++) {
            if (TextUtils.equals(mMemberList.get(i).getUserId(),member.getUserId())){
                index =i;
                break;
            }else {
                index =-1;
            }
        }
        if (index >=0){
            mMemberList.get(index).update(member);
        }else {
            mMemberList.add(member);
        }
    }

    public void removeMember(String userId) {
        Member member = new Member(userId);
        mMemberList.remove(member);
    }

    public Member getMember(String userId) {
        for (Member member : mMemberList) {
            if (TextUtils.equals(userId, member.getUserId())) {
                return member;
            }
        }
        return null;
    }

    public String getName(String userId){
        Member member =getMember(userId);
        if (member==null){
            return userId;
        }
        if (!TextUtils.isEmpty(member.getName())){
            return member.getName();
        }
        return userId;
    }


    public boolean isAnchor(String userId){
        return TextUtils.equals(userId,anchorId);
    }

    public boolean isMySelf(String userId){
        return TextUtils.equals(userId, SpUtil.getString(Constants.UID));
    }
}
