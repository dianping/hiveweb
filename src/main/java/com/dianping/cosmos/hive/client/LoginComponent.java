package com.dianping.cosmos.hive.client;

import java.util.Date;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class LoginComponent {
	private static final int COOKIE_TIMEOUT = 1000 * 60 * 60 * 12;

	private static String tokenid = Cookies.getCookie("tokenid") != null ? Cookies
			.getCookie("tokenid") : "";
	private static String username = Cookies.getCookie("username") != null ? Cookies
			.getCookie("username") : "";
	private static String realuser = Cookies.getCookie("realuser") != null ? Cookies
			.getCookie("realuser") : "";

	public static String getTokenid() {
		return tokenid;
	}

	public static String getUsername() {
		return username;
	}

	public static String getRealuser() {
		return realuser;
	}
	
	public static void setCookies(String username, String realuser, String tokenid, Date addtime) {
		Date expires = new Date((new Date()).getTime() + COOKIE_TIMEOUT);
		Cookies.setCookie("username", username, expires);
		Cookies.setCookie("realuser", realuser, expires);
		Cookies.setCookie("tokenid", tokenid, expires);
		Cookies.setCookie("addtime", addtime.toString(), expires);
	}
	
	public static void removeCookies() {
		Cookies.removeCookie("username");
		Cookies.removeCookie("realuser");
		Cookies.removeCookie("tokenid");
		Cookies.removeCookie("addtime");
	}

	public static void cleanup() {
		removeCookies();
		Window.alert("登陆失败!");
		Window.Location.assign("/index.html");
	}

	public native void redirect(String URL)
	/*-{
		$wnd.location = URL;
	}-*/;
}
