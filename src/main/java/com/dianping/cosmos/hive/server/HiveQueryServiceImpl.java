package com.dianping.cosmos.hive.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.TableSchemaBo;
import com.dianping.cosmos.hive.client.service.HiveQueryService;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;
import com.dianping.cosmos.hive.server.store.domain.QueryHistory;
import com.dianping.cosmos.hive.server.store.service.QueryHistoryService;
import com.dianping.cosmos.hive.shared.util.StringUtils;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
@Service("HiveQuery")
public class HiveQueryServiceImpl extends RemoteServiceServlet implements
		HiveQueryService {
	private static final Log logger = LogFactory
			.getLog(HiveQueryServiceImpl.class);

	@Autowired
	private QueryHistoryService queryHistoryService;

	@Autowired
	@Qualifier("HiveCmdLineQueryEngine")
	private IQueryEngine queryEngine;
	
	@Autowired
	private HiveJdbcClient hiveJdbcClient;

	@Override
	public List<String> getDatabases(String tokenid) {
		return hiveJdbcClient.getDatabases(tokenid);
	}

	@Override
	public List<String> getLatestNQuery() {
		List<String> hqls = queryHistoryService.selectLastNQuery();
		
		if (logger.isDebugEnabled()){
			for (String string : hqls) {
				logger.debug(string);
			}
		}
		
		if (hqls != null && hqls.size() > 0) {
			return hqls;
		}
		return null;
	}

	@Override
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input) {
		if (input == null)
			return null;
		HiveQueryOutput result = queryEngine.getQueryResult(new HiveQueryInput(
				input));
		
		// insert query history DB
		String resultLocation = "";
		if (result.getStoreFileLocation() != null){
			resultLocation = result.getStoreFileLocation(); 
		}
		
		QueryHistory history = new QueryHistory();
		history.setHql(input.getHql());
		history.setUsername(input.getUsername());
		history.setAddtime(new Date(input.getTimestamp()));
		history.setFilename(resultLocation);
		queryHistoryService.insertQueryHistory(history);
		
		HiveQueryOutputBo bo = result.toHiveQueryOutputBo();
		return bo;
	}

	@Override
	public String getQueryStatus(String username, long timestamp) {
		return queryEngine.getQueryStatus(username, timestamp);
	}

	@Override
	public void stopQuery(String username, long timestamp) {
		queryEngine.stopQuery(username, timestamp);
	}

	@Override
	public String getQueryPlan(String tokenid, String hql, String database) {
		return hiveJdbcClient.getQueryPlan(tokenid, StringUtils.preprocessQuery(hql), database);
	}

	@Override
	public List<String> getTables(String tokenid, String database) {
		return hiveJdbcClient.getTables(tokenid, database);
	}

	@Override
	public List<TableSchemaBo> getTableSchema(String tokenid, String database,
			String table) {
		return hiveJdbcClient.getTableSchema(tokenid, database, table);
	}

	@Override
	public String getTableSchemaDetail(String tokenid, String database,
			String table) {
		return hiveJdbcClient.getTableSchemaDetail(tokenid, database, table);
	}

	@Override
	public List<QueryHistoryBo> getQueryHistory(String username) {
		List<QueryHistoryBo> qhbs  = new ArrayList<QueryHistoryBo>();
		
		List<QueryHistory> qhs = queryHistoryService.selectQueryHistoryByUsername(username);
		for (QueryHistory qh : qhs) {
			QueryHistoryBo o = new QueryHistoryBo();
			o.setUsername(qh.getUsername());
			o.setAddtime(qh.getAddtime());
			o.setHql(qh.getHql());
			o.setFilename(qh.getFilename());
			
			qhbs.add(o);
		}
		return qhbs;
	}
}
