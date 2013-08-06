package com.dianping.cosmos.hive.client;

import java.util.ArrayList;
import java.util.List;

public final class Constants {
	public final static List<String> ALLOWED_USER = new ArrayList<String>();
	public final static List<String> ALLOWED_GROUP = new ArrayList<String>();
	
	static {
		ALLOWED_USER.add("yukang.chen");
		ALLOWED_USER.add("yi.zhang");
		ALLOWED_USER.add("yix.zhang");
		ALLOWED_USER.add("ming.fang");
		ALLOWED_USER.add("erik.fang");
		ALLOWED_USER.add("guangbin.zhu");
		ALLOWED_USER.add("yifan.cao");
		ALLOWED_USER.add("ben.lin");
		
		ALLOWED_GROUP.add("dataday_present");
		ALLOWED_GROUP.add("ba");
		ALLOWED_GROUP.add("ba1");
		ALLOWED_GROUP.add("online_report");
	}
}
