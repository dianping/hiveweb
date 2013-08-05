package com.dianping.cosmos.hive.client;

import com.google.gwt.user.cellview.client.Column;

public class IndexedColumn extends Column<String[], TextAndTooltip> {
	private final int index;

	public IndexedColumn(int index) {
		super(new TooltipCell());
		this.index = index;
	}

	@Override
	public TextAndTooltip getValue(String[] object) {
		String value = object[this.index];
		String shortValue = value.length() > 40 ? value.substring(0, 40)
				+ "..." : value;
		return new TextAndTooltip(shortValue, value);
	}
}
