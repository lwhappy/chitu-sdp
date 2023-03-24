package com.chitu.cloud.model;


import java.io.Serializable;

/**
 * 用户信息
 * @author liheng
 * @since 1.0
 */
public class UserInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	public static final String KEY_ID = "uid";
	public static final String KEY_USERNAME = "username";

	/**
	 * 用户ID
	 */
	private String id;
    /**
     * 用户工号
     */
    private String userNumber;

	/**
	 * 用户中文名
	 */
	private String username;

    /**
     * 用户英文文名
     */
    private String userNameEn;

    /**
     * 用户呢称
     */
    private String nickname;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public String getUserNameEn() {
        return userNameEn;
    }

    public UserInfo setUserNameEn(String userNameEn) {
        this.userNameEn = userNameEn;
        return this;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public UserInfo setUserNumber(String userNumber) {
        this.userNumber = userNumber;
        return this;
    }

    public static String getHeaderName(String key) {
        return "X-" + key;
    }

}
