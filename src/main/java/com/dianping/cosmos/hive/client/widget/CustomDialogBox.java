package com.dianping.cosmos.hive.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HTML;

public class CustomDialogBox extends DialogBox {

	private DockPanel dock = new DockPanel();
	private HTML details;
	private Button closeButton;

	public CustomDialogBox(String text) {
		this.setGlassEnabled(true);
		this.setAnimationEnabled(true);

		details = new HTML();
		details.setHTML(text);

		dock = new DockPanel();
		dock.setSpacing(4);
		closeButton = new Button("close", new ClickHandler() {
			public void onClick(ClickEvent event) {
				hide();
			}
		});

		dock.add(closeButton, DockPanel.SOUTH);
		dock.add(details, DockPanel.NORTH);
		dock.setCellHorizontalAlignment(closeButton, DockPanel.ALIGN_RIGHT);
		dock.setWidth("100%");
		setWidget(dock);

	}

	public void setBodyContent(String text) {
		this.details.setHTML(text);
	}
}
