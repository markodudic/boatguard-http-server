package com.boatguard.boatguard.dao;

public class Error {

	public static final String LOGIN_ERROR = "Login error";
	public static final String LOGIN_ERROR_CODE = "1";
	public static final String LOGIN_ERROR_MSG = "Wrong login";

	public static final String REGISTER_ERROR = "Register error";
	public static final String REGISTER_ERROR_CODE = "2";
	public static final String REGISTER_ERROR_MSG = "This OBU/USERNAME is registered or not exists.";

	private String name;
	private String code;
	private String msg;
	
	public Error (String name, String code, String msg) {
		this.name = name;
		this.code = code;
		this.msg = msg;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
}
