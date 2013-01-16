package com.dianping.cosmos.hive.server.queryengine.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.shared.util.StringUtils;

public class JdbcQueryEngine implements IQueryEngine {

	@Autowired
	private HiveJdbcClient hiveJdbcClient;

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		return null;
	}

	@Override
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input) {
		String tokenid = input.getTokenid();
		String username = input.getUsername();
		String hql = StringUtils.preprocessQuery(input.getHql());
		String database = input.getDatabase();
		int resultLimit = input.getResultLimit();
		Boolean isStoreFile = input.isStoreResult();
		String resultLocation = input.getResultLocation();
		long timestamp = input.getTimestamp();
		

		return hiveJdbcClient.getQueryResult(tokenid, username, database, hql,
				resultLimit, isStoreFile, resultLocation, timestamp);
	}

	// under the current status, it can't get the query status via hive jdbc
	@Override
	public String getQueryStatus(String queryId) {
		return null;
	}

	// can't stop query via jdbc
	@Override
	public Boolean stopQuery(String queryId) {
		return true;
	}
}
