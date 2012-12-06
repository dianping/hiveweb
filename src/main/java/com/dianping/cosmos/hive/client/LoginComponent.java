package com.dianping.cosmos.hive.client;

import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

public class LoginComponent {

	private String tokenid = "";
	private String username = "";

	public LoginComponent() {
		username = Cookies.getCookie("username") != null ? Cookies
				.getCookie("username") : "";
		tokenid = Cookies.getCookie("tokenid") != null ? Cookies
				.getCookie("tokenid") : "";
	}

	public String getTokenid() {
		return tokenid;
	}

	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void cleanup() {
		Window.alert("Login Failed !");
		LoginPage.removeCookies();
		Window.Location.assign("/index.html");
	}

	public native void redirect(String URL)
	/*-{
		$wnd.location = URL;
	}-*/;
}
