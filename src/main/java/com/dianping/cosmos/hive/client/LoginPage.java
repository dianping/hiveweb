package com.dianping.cosmos.hive.client;

import java.util.Date;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.LoginTokenBo;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@UrlPatternEntryPoint(value = "index([^.]*).html(\\\\?.*)?")
public class LoginPage implements EntryPoint {
	private static LoginViewUiBinder uiBinder = GWT
			.create(LoginViewUiBinder.class);

	private static final int COOKIE_TIMEOUT = 1000 * 60 * 60 * 12;

	interface LoginViewUiBinder extends UiBinder<Widget, LoginPage> {
	}

	@UiField
	TextBox username;
	@UiField
	PasswordTextBox password;
	@UiField
	Button submit_button;
	@UiField
	SimplePanel message;

	private final LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	@Override
	public void onModuleLoad() {
		Widget widget = uiBinder.createAndBindUi(this);
		RootPanel.get("majorPart").add(widget);

		this.bind();
	}

	private void bind() {
		submit_button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final String u = username.getValue();
				final String p = password.getValue();
				if (!u.equals("") && !p.equals("")) {
					loginService.authenticate(u, p,
							new AsyncCallback<LoginTokenBo>() {

								@Override
								public void onSuccess(LoginTokenBo result) {
									if (result == null) {
										message.setWidget(new HTML(
												"login failed"));
									} else {
										setCookies(u, result.getTokenid(),
												result.getAddtime());
										//redirect2("home.html?gwt.codesvr=127.0.0.1:9997");
										//redirect("home.html?gwt.codesvr=127.0.0.1:9997");
									}
								}

								@Override
								public void onFailure(Throwable caught) {
									message.setWidget(new HTML("login failed"));
								}
							});
				} else {
					message.setWidget(new HTML(
							"username or password can not be empty!"));
				}
			}
		});
	}

	public void setCookies(String username, String tokenid, Date addtime) {
		Date expires = new Date((new Date()).getTime() + COOKIE_TIMEOUT);

		Cookies.setCookie("username", username, expires);
		Cookies.setCookie("tokenid", tokenid, expires);
		Cookies.setCookie("addtime", addtime.toString(), expires);
	}

	public static void removeCookies() {
		Cookies.removeCookie("username");
		Cookies.removeCookie("tokenid");
		Cookies.removeCookie("addtime");
	}

	public static native void redirect(String url)/*-{
		$wnd.location = url;
	}-*/;

	public static native void redirect2(String url)
	/*-{
		$wnd.location.replace(url);
	}-*/;

}
