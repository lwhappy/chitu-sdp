package com.chitu.cloud.utils;

import com.chitu.cloud.model.UserInfo;

/**
 * @author liheng
 * @since 1.0
 */
public class Context {

    private String userId;
    private UserInfo user;

    public String getUserId() {
        return userId;
    }

    public UserInfo getUser() {
        if (user == null) {
            user = new UserInfo();
        }
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
        if (this.user != null) {
            this.userId = this.user.getId();
        }
    }

}
