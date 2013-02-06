package com.dianping.cosmos.hive.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CustomPopupPanel extends PopupPanel{
	HTML message = new HTML();
	Button closeButton = new Button("close", new ClickHandler() {
		
		@Override
		public void onClick(ClickEvent event) {
			hide();
		}
	});
	
	public CustomPopupPanel(){
		super(false);
		setAnimationEnabled(true);
		setGlassEnabled(true);
		
		VerticalPanel PopUpPanelContents = new VerticalPanel();
		setStyleName("my-popup");
	    PopUpPanelContents = new VerticalPanel();
	    setTitle("Title");
	    message.setStyleName("my-popup-message");
	    SimplePanel holder = new SimplePanel();
	    holder.add(closeButton);
	    holder.setStyleName("my-popup-footer");
	    PopUpPanelContents.add(message);
	    PopUpPanelContents.add(holder);
	    setWidget(PopUpPanelContents);
	}
	
	public void setMessage(String msg) {
		this.message.setText(msg);
	}

}
