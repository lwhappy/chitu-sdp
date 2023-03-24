package com.chitu.cloud.model;

import static com.chitu.cloud.model.ResponseCode.UNKOWN_EXCEPTION;

/**
 * 响应结果
 * @author liheng
 * @since 1.0
 */

public class ResponseData<T> {
	/** 错误或者成功代码 */
	private int code;
	/** 错误描述 */
	private String msg;
	/** 响应结果*/
	private T data;
	private String traceId;

	/**
	 * 构造函数
	 */
	public ResponseData() {
		this(UNKOWN_EXCEPTION);
	}
	/**
	 * 构造函数
	 * @param code    错误或者成功代码
	 * @param message 错误描述
	 */
	public ResponseData(int code, String message) {
		this.code = code;
		this.msg = message;
	}
	
	/**
	 * 构造函数
	 * @param code    错误或者成功代码
	 * @param message 错误描述
	 * @param data    响应结果
	 * 
	 */
	public ResponseData(int code, String message, T data) {
		this.code = code;
		this.msg = message;
		this.data = data;
	}
	
	/**
	 * 构造函数
	 */
	public ResponseData(AppCode appCode) {
		this.code = appCode.getCode();
		this.msg = appCode.getMessage();
	}


	/**
	 * 构造函数
	 */
	public ResponseData(AppCode appCode, T data) {
		this.code = appCode.getCode();
		this.msg = appCode.getMessage();
		this.data = data;
	}


	/**
	 * @return the code
	 */
	public int getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(int code) {
		this.code = code;
	}
	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}
	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}
	/**
	 * @param traceId the traceId to set
	 */
	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}
	/**
	 * @return the traceId
	 */
	public String getTraceId() {
		return traceId;
	}
	/**
	 * @return the data
	 */
	public T getData() {
		return data;
	}
	/**
	 * @param data the data to set
	 */
	public ResponseData setData(T data) {
		this.data = data;
		return this;
	}

	public void responseCode(int code, String message) {
        this.code = code;
        this.msg = message;
    }

	public void ok() {
        this.code = ResponseCode.SUCCESS.getCode();
        this.msg = ResponseCode.SUCCESS.getMessage();
	}

    /**
     * 成功返回true
     * @return
     */
	public boolean isSuccess() {
		return this.code == ResponseCode.SUCCESS.getCode();
	}
}
