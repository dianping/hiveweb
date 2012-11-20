package com.dianping.cosmos.hive.client.service;

import java.util.List;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.TableSchemaBo;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/HiveQuery")
public interface HiveQueryService extends RemoteService {

	public List<String> getDatabases(String tokenid);
	
	public List<String> getTables(String tokenid, String database);
	
	public List<TableSchemaBo> getTableSchema(String tokenid, String database, String table);
	
	public String getTableSchemaDetail(String tokenid, String database, String table);
	
	public List<String> getLatestNQuery();
	
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input);
	
	public String getQueryStatus(String username, long timestamp);
	
	public void stopQuery(String username, long timestamp);
	
	public String getQueryPlan(String tokenid, String hql, String database);
	
	public List<QueryHistoryBo> getQueryHistory(String username);
	
}
