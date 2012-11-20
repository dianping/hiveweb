package com.dianping.cosmos.hive.server.queryengine.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cosmos.hive.server.HiveJdbcClient;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;

public class JdbcQueryEngine implements IQueryEngine {

	@Autowired
	private HiveJdbcClient hiveJdbcClient;

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		String tokenid = input.getTokenid();
		String username = input.getUsername();
		String hql = input.getOriginalHql();
		String database = input.getDatabase();
		int resultLimit = input.getResultLimit();
		Boolean isStoreFile = input.isStoreResult();
		long timestamp = input.getTimestamp();

		return hiveJdbcClient.getQueryResult(tokenid, username, database, hql,
				resultLimit, isStoreFile, timestamp);
	}

	@Override
	public String getQueryStatus(String username, long timestamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryOutputLocation(String username, long timestamp) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void stopQuery(String username, long timestamp) {
		// TODO Auto-generated method stub
	}

}
