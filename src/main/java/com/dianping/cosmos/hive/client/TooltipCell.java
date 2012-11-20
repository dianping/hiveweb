package com.dianping.cosmos.hive.client;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class TooltipCell extends AbstractCell<TextAndTooltip> {

	@Override
	public void render(Context context, TextAndTooltip textAndTip, SafeHtmlBuilder builder) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span title='");
		sb.append(textAndTip.getTooltip());
		sb.append("'>");
		sb.append(textAndTip.getText());
		sb.append("</span>");
		builder.appendHtmlConstant(sb.toString());
	}
}
