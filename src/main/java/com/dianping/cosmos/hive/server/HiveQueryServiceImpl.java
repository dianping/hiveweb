package com.dianping.cosmos.hive.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.FieldSchemaBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.client.bo.QueryErrorBo;
import com.dianping.cosmos.hive.client.bo.QueryFavoriteBo;
import com.dianping.cosmos.hive.client.bo.QueryHistoryBo;
import com.dianping.cosmos.hive.client.bo.ResultStatusBo;
import com.dianping.cosmos.hive.client.service.HiveQueryService;
import com.dianping.cosmos.hive.server.mail.MailService;
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
	private static final Log LOG = LogFactory
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

	@Autowired
	private MailService mailService;

	@Override
	public List<String> getDatabases(String tokenid) {

		return hiveJdbcClient.getDatabases(tokenid);
	}

	@Override
	public List<String> getLatestNQuery() {
		List<String> hqls = queryHistoryService.selectLastNQuery();

		if (LOG.isDebugEnabled()) {
			for (String string : hqls) {
				LOG.debug(string);
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
		long startTime = System.currentTimeMillis();
		if (queryEngine instanceof JdbcQueryEngine) {
			output = queryEngine.getQueryResult(input);
		} else if (queryEngine instanceof HiveCmdLineQueryEngine) {
			output = queryEngine.getQueryResult(input);
		} else {
			LOG.error("No such queryEngine implemented "
					+ queryEngine.getClass());
		}
		long duration = (System.currentTimeMillis() - startTime) / 1000;

		if (output != null && output.getSuccess() == true) {
			// insert query history DB
			String resultFileLocation = "";
			if (!StringUtils.isBlank(output.getResultFileAbsolutePath())) {
				resultFileLocation = output.getResultFileAbsolutePath();
			}

			QueryHistory history = new QueryHistory();
			history.setHql(input.getHql());
			history.setUsername(input.getRealuser());
			history.setAddtime(new Date(input.getTimestamp()));
			history.setFilename(resultFileLocation);
			history.setMode(input.getEngineMode());
			history.setExectime(duration);
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
			o.setMode(qh.getMode());
			o.setExectime(qh.getExectime());
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
			return hiveJdbcClient.executeHiveQuery(tokenid, hql);
		}
		ResultStatusBo r = new ResultStatusBo(false,
				"the input hql can not be empty");
		return r;
	}

	@Override
	public ResultStatusBo uploadTableFile(String tokenid, String username,
			String dbname, String tablename, String filelocation,
			Boolean overwrite, String partionCond) {
		ResultStatusBo rs = new ResultStatusBo(false, "");
		StringBuilder sb = new StringBuilder(150);
		sb.append("LOAD DATA LOCAL INPATH '").append(filelocation).append("' ");
		if (overwrite == true) {
			sb.append("OVERWRITE ");
		}
		sb.append("INTO TABLE ").append(dbname).append(".").append(tablename);
		if (StringUtils.isNotBlank(partionCond)) {
			sb.append(" PARTITION (").append(partionCond).append(")");
		}

		String ticketCache = "/tmp/" + username + ".ticketcache";
		String exportKRB5Cmd = "export KRB5CCNAME=" + ticketCache;
		String hiveQueryCmd = "hive -e \"" + sb.toString() + "\"";
		String[] shellCmd = { "bash", "-c", exportKRB5Cmd + ";" + hiveQueryCmd };

		LOG.info("exec load data command:" + StringUtils.join(shellCmd, " "));
		ShellCommandExecutor shExec = new ShellCommandExecutor(shellCmd);

		try {
			shExec.execute();
		} catch (IOException e) {
			LOG.error("Error while uploading file : " + filelocation
					+ ", command:" + shellCmd + " , Exception: "
					+ e.getMessage());
			rs.setSuccess(false);
			rs.setMessage(e.getMessage());
		}
		if (shExec.getExitCode() == 0) {
			rs.setSuccess(true);
			FileUtils.deleteQuietly(new File(filelocation));
		}
		return rs;
	}

	@Override
	public Boolean submitQueryError(QueryErrorBo error) {
		StringBuilder sb = new StringBuilder();
		sb.append("username: \n").append(error.getUsername()).append("\n\n");
		sb.append("mode: \n").append(error.getMode()).append("\n\n");
		sb.append("sql: \n").append(error.getSql()).append("\n\n");
		sb.append("status: \n").append(error.getStatus());
		LOG.info("send hive bug mail from user " + error.getUsername());
		return mailService.sendMail(sb.toString());
	}
}