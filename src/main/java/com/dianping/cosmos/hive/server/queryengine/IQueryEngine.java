package com.dianping.cosmos.hive.server.queryengine;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;

public interface IQueryEngine {
	
	@Deprecated
	public HiveQueryOutput getQueryResult(HiveQueryInput input);
	
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input);
	
	public String getQueryStatus(String queryId);
	
	public Boolean stopQuery(String queryId);
	
}
