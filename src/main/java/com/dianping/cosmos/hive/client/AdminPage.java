package com.dianping.cosmos.hive.client;

import org.gwtmultipage.client.UrlPatternEntryPoint;

import com.dianping.cosmos.hive.client.bo.LoginTokenBo;
import com.dianping.cosmos.hive.client.service.LoginServiceAsync;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

@UrlPatternEntryPoint(value = "admin.html")
public class AdminPage extends LoginComponent implements EntryPoint {
	private static AdminViewUiBinder uiBinder = GWT
			.create(AdminViewUiBinder.class);

	interface AdminViewUiBinder extends UiBinder<Widget, AdminPage> {
	}

	@UiField
	TextBox username;
	@UiField
	PasswordTextBox password;
	@UiField
	Button submit_button;
	@UiField
	Button reset_button;
	@UiField
	SimplePanel message;

	private final LoginServiceAsync loginService = LoginServiceAsync.Util
			.getInstance();

	@Override
	public void onModuleLoad() {
		if (getTokenid() == null || getTokenid().equals("")) {
			drawPanel();
		} else {
			loginService.isAuthenticated(getTokenid(),
					new AsyncCallback<Boolean>() {

						@Override
						public void onSuccess(Boolean result) {
							if (result) {
								Window.Location.assign("/home.html");
							} else {
								drawPanel();
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							drawPanel();
						}
					});
		}
	}
	
	private void drawPanel() {
		Widget widget = uiBinder.createAndBindUi(this);
		RootPanel.get("majorPart").add(widget);
		this.bind();
	}

	private void bind() {
		password.addKeyPressHandler(new KeyPressHandler() {
			
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int keyPress = event.getCharCode();
				if (keyPress == KeyCodes.KEY_ENTER){
					login();
				}
			}
		});
		
		submit_button.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				login();
			}
		});
		
		reset_button.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				username.setValue("");
				password.setValue("");
			}
		});
	}
	
	private void login(){
		final String u = username.getValue();
		final String p = password.getValue();
		if (!u.equals("") && !p.equals("")) {
			loginService.authenticate(u, p,
					new AsyncCallback<LoginTokenBo>() {

						@Override
						public void onSuccess(LoginTokenBo result) {
							if (result == null) {
								message.setWidget(new HTML(
										"登陆失败!"));
							} else {
								setCookies(u, u, result.getTokenid(),
										result.getAddtime());
								Window.Location.assign("/home.html");
							}
						}

						@Override
						public void onFailure(Throwable caught) {
							message.setWidget(new HTML("登陆失败!"));
						}
					});
		} else {
			message.setWidget(new HTML("用户名和密码不能为空!"));
		}
	}
}