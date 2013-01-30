package com.dianping.cosmos.hive.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryFavoriteBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.FieldSchemaBo;
import com.dianping.cosmos.hive.client.bo.ResultStatusBo;
import com.dianping.cosmos.hive.client.service.HiveQueryService;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.server.queryengine.cmdline.HiveCmdLineQueryEngine;
import com.dianping.cosmos.hive.server.queryengine.jdbc.DataFileStore;
import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;
import com.dianping.cosmos.hive.server.queryengine.jdbc.JdbcQueryEngine;
import com.dianping.cosmos.hive.server.store.domain.QueryFavorite;
import com.dianping.cosmos.hive.server.store.domain.QueryHistory;
import com.dianping.cosmos.hive.server.store.service.QueryFavoriteService;
import com.dianping.cosmos.hive.server.store.service.QueryHistoryService;
import com.dianping.cosmos.hive.shared.util.StrUtils;
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
	private QueryFavoriteService queryFavoriteService;

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

		if (logger.isDebugEnabled()) {
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
		if (input == null) {
			return null;
		}

		String resultLocation = "";
		resultLocation = DataFileStore.getStoreFileAbsolutePath(
				input.getTokenid(), input.getUsername(), input.getDatabase(),
				input.getHql(), input.getTimestamp(), input.getQueryid());
		input.setResultLocation(resultLocation);

		HiveQueryOutputBo output = null;
		if (queryEngine instanceof JdbcQueryEngine) {
			output = queryEngine.getQueryResult(input);
		} else if (queryEngine instanceof HiveCmdLineQueryEngine) {
			output = queryEngine.getQueryResult(input);
		} else {
			logger.error("No such queryEngine implemented "
					+ queryEngine.getClass());
		}

		if (output != null  && "".equals(output.getErrorMsg())) {
			// insert query history DB
			String resultFileLocation = "";
			if (!StringUtils.isEmpty(output
					.getResultFileAbsolutePath())) {
				resultFileLocation = output.getResultFileAbsolutePath();
			}
			
			QueryHistory history = new QueryHistory();
			history.setHql(input.getHql());
			history.setUsername(input.getUsername());
			history.setAddtime(new Date(input.getTimestamp()));
			history.setFilename(resultFileLocation);
			queryHistoryService.insertQueryHistory(history);
		}

		return output;
	}

	@Override
	public String getQueryStatus(String queryId) {
		return queryEngine.getQueryStatus(queryId);
	}

	@Override
	public String getQueryPlan(String tokenid, String hql, String database) {
		return hiveJdbcClient.getQueryPlan(tokenid,
				StrUtils.preprocessQuery(hql), database);
	}

	@Override
	public List<String> getTables(String tokenid, String database) {
		return hiveJdbcClient.getTables(tokenid, database);
	}

	@Override
	public List<FieldSchemaBo> getTableSchema(String tokenid, String database,
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
		List<QueryHistoryBo> qhbs = new ArrayList<QueryHistoryBo>();

		List<QueryHistory> qhs = queryHistoryService
				.selectQueryHistoryByUsername(username);
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

	@Override
	public Boolean stopQuery(String queryId) {
		return queryEngine.stopQuery(queryId);
	}

	@Override
	public Boolean saveQuery(String username, String queryName, String hql) {
		QueryFavorite qf = new QueryFavorite();
		qf.setUsername(username);
		qf.setQueryName(queryName);
		qf.setHql(hql);
		qf.setAddtime(new Date());
		queryFavoriteService.insertQueryFavorite(qf);
		return true;
	}

	@Override
	public List<QueryFavoriteBo> getFavoriteQuery(String username) {
		List<QueryFavorite> queryFavs = queryFavoriteService
				.selectQueryFavoriteByUsername(username);
		List<QueryFavoriteBo> queryFavsBos = new ArrayList<QueryFavoriteBo>();
		if (queryFavs != null && queryFavs.size() > 0) {
			for (QueryFavorite qf : queryFavs) {
				QueryFavoriteBo q = new QueryFavoriteBo();
				q.setQueryName(qf.getQueryName());
				q.setHql(qf.getHql());
				queryFavsBos.add(q);
			}
		}
		return queryFavsBos;
	}

	@Override
	public ResultStatusBo createTable(String tokenid, String hql) {
		if (!StringUtils.isBlank(tokenid) && !StringUtils.isBlank(hql)) {
			return hiveJdbcClient.createTable(tokenid, hql);
		}
		ResultStatusBo r = new ResultStatusBo();
		r.setSuccess(false);
		r.setMessage("查询语句不能为空");
		return r;
	}
}
