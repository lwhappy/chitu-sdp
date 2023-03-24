package com.chitu.cloud.exception;

import com.chitu.cloud.model.AppCode;

/**
 * 应用错误
 * @author liheng
 * @since 1.0
 */
public class ApplicationException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	/** 错误代码 */
	private AppCode appCode;
	/**
	 * 构造函数
	 */
	public ApplicationException() {
	}
	/**
	 * 构造函数
	 * @param appCode	应用代码
	 */
	public ApplicationException(AppCode appCode) {
		super(appCode.getMessage());
		this.appCode = appCode;
	}

	/**
	 * 构造函数
	 * @param appCode	应用代码
	 * @param msgArgs   格式化消息参数，请参考{@linkplain String String.format}
	 *
	 */
	public ApplicationException(AppCode appCode, Object... msgArgs) {
		super(String.format(appCode.getMessage(), msgArgs));
		this.appCode = appCode;
	}

	/**
	 * 构造函数
	 * @param code		错误代码
	 * @param message	错误描述
	 */
	public ApplicationException(int code, String message) {
		this(code, message, null);
	}

	/**
	 * 构造函数
	 * @param code		错误代码
	 * @param message	错误描述
	 * @param cause		错误对象
	 */
	public ApplicationException(int code, String message, Throwable cause) {
		super(message, cause);
		this.appCode = new DefaultAppCode(code, message);
	}

	public AppCode getAppCode() {
		return appCode;
	}

	public void setAppCode(AppCode appCode) {
		this.appCode = appCode;
	}

	class DefaultAppCode implements AppCode {
		private int code;
		private String message;

		public DefaultAppCode(int code, String message) {
			this.setCode(code);
			this.setMessage(message);
		}

		@Override
		public int getCode() {
			return code;
		}

		@Override
		public void setCode(int code) {
			this.code = code;
		}

		@Override
		public String getMessage() {
			return message;
		}

		@Override
		public void setMessage(String message) {
			this.message = message;
		}
	}

}