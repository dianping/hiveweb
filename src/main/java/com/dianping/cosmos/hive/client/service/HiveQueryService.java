package com.dianping.cosmos.hive.client.service;

import java.util.List;

import com.dianping.cosmos.hive.client.bo.FieldSchemaBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryFavoriteBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.ResultStatusBo;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("springGwtServices/HiveQuery")
public interface HiveQueryService extends RemoteService {

	public List<String> getDatabases(String tokenid);
	
	public List<String> getTables(String tokenid, String database);
	
	public List<FieldSchemaBo> getTableSchema(String tokenid, String database, String table);
	
	public String getTableSchemaDetail(String tokenid, String database, String table);
	
	public List<String> getLatestNQuery();
	
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input);
	
	public String getQueryStatus(String queryId);
	
	public Boolean stopQuery(String queryId);
	
	public String getQueryPlan(String tokenid, String hql, String database);
	
	public List<QueryHistoryBo> getQueryHistory(String username);
	
	public Boolean saveQuery(String username, String queryName, String hql);
	
	public List<QueryFavoriteBo> getFavoriteQuery(String username);
	
	public ResultStatusBo createTable(String tokenid, String hql);
	
	public ResultStatusBo uploadTableFile(String tokenid, String username, String dbname, String tablename, String filelocation, Boolean overwrite , String partionCond);
}
