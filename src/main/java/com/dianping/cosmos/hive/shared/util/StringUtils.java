package com.dianping.cosmos.hive.shared.util;

import java.util.List;

public final class StringUtils {
	private StringUtils(){
	}

	public static String listToString(List<String> stringList){
		StringBuilder ret = new StringBuilder();
		if (stringList != null && stringList.size() > 0){
			for (int i = 0; i < stringList.size(); i++) {
				ret.append(stringList.get(i)).append("<br><br>");
			}
		}
		return ret.toString();
	}
}
