package com.dianping.cosmos.hive.client;

public class TextAndTooltip {
	String text;
	String tooltip;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

	public TextAndTooltip(String text, String tooltip) {
		super();
		this.text = text;
		this.tooltip = tooltip;
	}

	public TextAndTooltip(String fullText, int textLen) {
		this.tooltip = fullText;
		this.text = fullText.length() > textLen ? fullText
				.substring(0, textLen) + "..." : fullText;
	}
}
