package org.ar.ar_audiomic.model;

public class Member {

    private String userId;
    private String name;

    public Member(String userId) {
        this.userId = userId;
    }

    public Member(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public void update(Member member) {
        this.name = member.name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
