package com.dianping.cosmos.hive.client.widget;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;

public class AutoCompleteTextArea extends TextArea implements KeyUpHandler,
	ChangeHandler {

	protected PopupPanel choicesPopup = new PopupPanel(true);
	protected ListBox choices = new ListBox();
	protected CompletionItems items = new SimpleAutoCompletionItems(
			new String[] {});
	protected boolean popupAdded = false;
	protected String typedText = "";
	protected boolean visible = false;

	protected int posy = -1;

	/**
	 * Default Constructor
	 * 
	 */
	public AutoCompleteTextArea() {
		super();
		this.addKeyUpHandler(this);
		choices.addChangeHandler(this);
		this.setStyleName("AutoCompleteTextArea");
		choicesPopup.add(choices);
		choicesPopup.addStyleName("AutoCompleteChoices");
		choices.setStyleName("list");
	}

	/**
	 * Sets an "algorithm" returning completion items You can define your own
	 * way how the textbox retrieves autocompletion items by implementing the
	 * CompletionItems interface and setting the according object
	 * 
	 * @see SimpleAutoCompletionItem
	 * @param items
	 *            CompletionItem implementation
	 */
	public void setCompletionItems(CompletionItems items) {
		this.items = items;
	}

	/**
	 * Returns the used CompletionItems object
	 * 
	 * @return CompletionItems implementation
	 */
	public CompletionItems getCompletionItems() {
		return this.items;
	}

	// add selected item to textarea
	protected void complete() {
		if (choices.getItemCount() > 0) {
			String text = this.getText();
			text = text.substring(0, text.length() - typedText.length() - 1);
			text += choices.getItemText(choices.getSelectedIndex());
			this.setText(text);
			this.setFocus(true);
		}

		choices.clear();
		choicesPopup.hide();
	}

	@Override
	public void onChange(ChangeEvent event) {
		complete();
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {
		int keyCode = event.getNativeKeyCode();
		if (keyCode == KeyCodes.KEY_DOWN) {
			int selectedIndex = choices.getSelectedIndex();
			selectedIndex++;
			if (selectedIndex > choices.getItemCount()) {
				selectedIndex = 0;
			}
			choices.setSelectedIndex(selectedIndex);
			return;
		}

		if (keyCode == KeyCodes.KEY_UP) {
			int selectedIndex = choices.getSelectedIndex();
			selectedIndex--;
			if (selectedIndex < 0) {
				selectedIndex = choices.getItemCount();
			}
			choices.setSelectedIndex(selectedIndex);
			return;
		}

		if (keyCode == KeyCodes.KEY_ENTER) {
			if (visible) {
				complete();
			}
			return;
		}

		if (keyCode == KeyCodes.KEY_ESCAPE) {
			choices.clear();
			choicesPopup.hide();
			visible = false;
			return;
		}

		String text = this.getText();
		String[] matches = new String[] {};

		String[] words = text.split(" |\n|\r");
		text = words[words.length - 1];
		typedText = text;

		if (text.length() > 0) {
			matches = items.getCompletionItems(text);
		}

		if (matches.length > 0) {
			choices.clear();

			for (int i = 0; i < matches.length; i++) {
				choices.addItem(matches[i]);
			}

			// if there is only one match and it is what is in the
			// text field anyways there is no need to show autocompletion
			if (matches.length == 1 && matches[0].compareTo(text) == 0) {
				choicesPopup.hide();
			} else {
				choices.setSelectedIndex(0);
				choices.setVisibleItemCount(matches.length + 1);

				if (!popupAdded) {
					RootPanel.get().add(choicesPopup);
					popupAdded = true;
				}
				choicesPopup.show();
				visible = true;
				int nposy = this.getAbsoluteTop() + this.getOffsetHeight();
				
				if (posy < 0 || nposy > posy) {
					posy = nposy;
				}
				choicesPopup.setPopupPosition(this.getAbsoluteLeft() + this.getOffsetWidth(), this.getAbsoluteTop());
				choices.setWidth(this.getOffsetWidth() / 3 + "px");
			}

		} else {
			choicesPopup.hide();
			visible = false;
		}
	}
}
