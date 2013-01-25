package com.dianping.cosmos.hive.client.widget;

import java.util.ArrayList;

public class SimpleAutoCompletionItems implements CompletionItems {
	private String[] completions;

	public SimpleAutoCompletionItems(String[] items) {
		completions = items;
	}

	@Override
	public String[] getCompletionItems(String match) {
		ArrayList<String> matches = new ArrayList<String>();
		for (int i = 0; i < completions.length; i++) {
			if (completions[i].toLowerCase().startsWith(match.toLowerCase())) {
				matches.add(completions[i]);
			}
		}
		String[] returnMatches = new String[matches.size()];
		for (int i = 0; i < matches.size(); i++) {
			returnMatches[i] = matches.get(i);
		}
		return returnMatches;
	}
}
