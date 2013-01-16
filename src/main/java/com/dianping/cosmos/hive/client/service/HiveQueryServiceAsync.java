package com.dianping.cosmos.hive.client.service;

import java.util.List;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryFavoriteBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.TableSchemaBo;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface HiveQueryServiceAsync {
	public void getLatestNQuery(AsyncCallback<List<String>> callback);

	public void getDatabases(String tokenid, AsyncCallback<List<String>> callback);
	
	public void getTables(String tokenid, String database, AsyncCallback<List<String>> callback);
	
	public void getTableSchema(String tokenid, String database, String table, AsyncCallback<List<TableSchemaBo>> callback);
	
	public void getTableSchemaDetail(String tokenid, String database, String table, AsyncCallback<String> callback);
	
	public void getQueryResult(HiveQueryInputBo input, AsyncCallback<HiveQueryOutputBo> asyncCallback);
	
	public void getQueryStatus(String queryId, AsyncCallback<String> callback);

	public void stopQuery(String queryId, AsyncCallback<Boolean> callback);
	
	public void getQueryPlan(String tokenid, String hql, String database, AsyncCallback<String> callback);
	
	public void getQueryHistory(String username, AsyncCallback<List<QueryHistoryBo>> callback);
	
	public void saveQuery(String username, String queryName, String hql, AsyncCallback<Boolean> callback);
	
	public void getFavoriteQuery(String username, AsyncCallback<List<QueryFavoriteBo>> callback);
	
	public static final class Util {
		private static HiveQueryServiceAsync instance;

		public static final HiveQueryServiceAsync getInstance() {
			if (instance == null) {
				instance = (HiveQueryServiceAsync) GWT.create(HiveQueryService.class);
			}
			return instance;
		}

		private Util() {
		}
	}

}
