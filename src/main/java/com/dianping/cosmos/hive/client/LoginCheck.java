package com.dianping.cosmos.hive.client;

import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LoginCheck {
	private static Boolean isValidToken = false;
	private static Boolean hasReturnValue = false;

	private final static LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	public static Boolean verifyTokenStatus() {
		String tokenid = Cookies.getCookie("tokenid");
		if (tokenid == null) {
			return false;
		} else {
			loginService.isAuthenticated(tokenid, new AsyncCallback<Boolean>() {

				@Override
				public void onSuccess(Boolean result) {
					isValidToken = result;
					hasReturnValue = true;
				}

				@Override
				public void onFailure(Throwable caught) {
					caught.printStackTrace();
					isValidToken = false;
					hasReturnValue = true;
				}
			});
		}

		return isValidToken;
	}

	public native void redirect(String URL)
	/*-{
		$wnd.location = URL;
	}-*/;
}
