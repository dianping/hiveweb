package com.dianping.cosmos.hive.server.queryengine.jdbc;

import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.shared.util.StringUtils;

public class JdbcQueryEngine implements IQueryEngine {

	@Autowired
	private HiveJdbcClient hiveJdbcClient;

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		String tokenid = input.getTokenid();
		String username = input.getUsername();
		String hql = StringUtils.preprocessQuery(input.getOriginalHql());
		String database = input.getDatabase();
		int resultLimit = input.getResultLimit();
		Boolean isStoreFile = input.isStoreResult();
		long timestamp = input.getTimestamp();

		return hiveJdbcClient.getQueryResult(tokenid, username, database, hql,
				resultLimit, isStoreFile, timestamp);
	}

	// under the current status, it can't get the query status via hive jdbc
	@Override
	public String getQueryStatus(String username, long timestamp) {
		return null;
	}

	// can't stop query via jdbc
	@Override
	public void stopQuery(String username, long timestamp) {
	}

}
