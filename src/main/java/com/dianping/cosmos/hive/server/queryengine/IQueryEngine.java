package com.dianping.cosmos.hive.server.queryengine;

public interface IQueryEngine {
	
	public HiveQueryOutput getQueryResult(HiveQueryInput input);
	
	public String getQueryStatus(String username, long timestamp);
	
	public void stopQuery(String username, long timestamp);
	
}
