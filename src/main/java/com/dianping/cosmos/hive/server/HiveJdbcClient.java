package com.dianping.cosmos.hive.server;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.dianping.cosmos.hive.client.bo.TableSchemaBo;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.shared.util.ReflectUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@Service
public class HiveJdbcClient {
	private static final Log logger = LogFactory.getLog(HiveJdbcClient.class);

	private static final int USER_SHOW_ROW_MAXIMUM_NUMBER = 300;
	private static final int FILE_STORE_ROW_MAXIMUM_NUMBER = 100000;

	private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";

	private static Cache<String, Connection> connCache = CacheBuilder
			.newBuilder().concurrencyLevel(4).maximumSize(10000)
			.expireAfterWrite(24, TimeUnit.HOURS)
			.removalListener(new RemovalListener<String, Connection>() {

				@Override
				public void onRemoval(RemovalNotification<String, Connection> rn) {
					logger.info(("tokenid:" + rn.getKey() + " Connection"
							+ rn.getValue() + " was removed from connCache"));
				}

			}).build();

	static {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException cfe) {
			logger.error("Hive Driver Not Found", cfe);
		}
	}

	public static void putTokenConn(String tokenid, Connection conn) {
		connCache.put(tokenid, conn);
	}

	public static Connection getTokenConn(String tokenid) {
		return connCache.getIfPresent(tokenid);
	}

	public static void removeConnectionByTokenid(String tokenid) {
		connCache.invalidate(tokenid);
	}

	public List<String> getDatabases(String tokenid) {
		List<String> dbs = new ArrayList<String>();
		Connection conn = connCache.getIfPresent(tokenid);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("show databases");
			while (rs.next()) {
				dbs.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("getDatabases" + e);
		}
		return dbs;
	}

	public List<String> getTables(String tokenid, String database) {
		List<String> tables = new ArrayList<String>();
		Connection conn = connCache.getIfPresent(tokenid);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute("use " + database);
			ResultSet rs = stmt.executeQuery("show tables");
			while (rs.next()) {
				tables.add(rs.getString(1));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return tables;
	}

	public List<TableSchemaBo> getTableSchema(String tokenid, String database,
			String table) {
		List<TableSchemaBo> tsbs = new ArrayList<TableSchemaBo>();
		Connection conn = connCache.getIfPresent(tokenid);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute("use " + database);
			ResultSet rs = stmt.executeQuery("desc " + table);
			while (rs.next()) {
				TableSchemaBo tsb = new TableSchemaBo();
				tsb.setFieldName(rs.getString(1));
				tsb.setFieldType(rs.getString(2));
				tsb.setFieldComment(rs.getString(3));
				logger.info(tsb);
				tsbs.add(tsb);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error(e);
		}
		return tsbs;
	}

	public String getTableSchemaDetail(String tokenid, String database,
			String table) {
		Connection conn = connCache.getIfPresent(tokenid);
		StringBuilder sb = new StringBuilder(400);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			logger.info("execute " + "use " + database);
			stmt.execute("use " + database);
			stmt.executeQuery("use " + database);
			ResultSet rs = stmt.executeQuery("desc formatted " + table);
			ResultSetMetaData rsm = rs.getMetaData();
			int columnCount = rsm.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (StringUtils.isBlank(rs.getString(i))) {
						continue;
					} else {
						sb.append(rs.getString(i)).append("\t");
					}
				}
				sb.append("\n");
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error(String.format("tokenid:{0},database:{1},table:{2}",
					tokenid, database, table), e);
		}
		return sb.toString();

	}

	public String getQueryPlan(String tokenid, String hql, String database) {
		Connection conn = connCache.getIfPresent(tokenid);
		StringBuilder sb = new StringBuilder(500);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery("use " + database);
			
			stmt.setFetchSize(Integer.MAX_VALUE);
			ResultSet rs = stmt.executeQuery("explain " + hql);
			if (rs.next()){
				Object value = ReflectUtils.getFieldValue(rs, "fetchedRows");
				List<String> fetchedRows = (List<String>) value;
				if (fetchedRows != null || fetchedRows.size() > 0){
					for (String row : fetchedRows) {
						sb.append(row).append("\n");
					}
				}else {
					sb.append("something wrong with hive query");
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			sb.append(e.toString());
			logger.error("db:" + database + " hql:" + hql, e);
		}
		logger.info("db:" + database + " query plan for hql:" + hql + " is " + sb.toString());
		return sb.toString();
	}
	
	public HiveQueryOutput getQueryResult(String tokenid, String username,
			String database, String hql, int resultLimit,
			Boolean isStoreFile, long timestamp) {
		HiveQueryOutput res = new HiveQueryOutput();
		Connection conn = connCache.getIfPresent(tokenid);

		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery("use " + database);
			ResultSet rs = stmt.executeQuery(hql);
			ResultSetMetaData rsm = rs.getMetaData();
			int columnCount = rsm.getColumnCount();
			List<String> columnNames = new ArrayList<String>(columnCount);

			StringBuilder sb = new StringBuilder(200);
			for (int i = 1; i <= columnCount; i++) {
				columnNames.add(rsm.getColumnName(i));
				if (logger.isDebugEnabled()) {
					sb.append(rsm.getColumnName(i)).append("\t")
							.append(rsm.getColumnTypeName(i)).append("\n");
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("resultset meta data is:" + sb.toString());
			}

			res.setTitleList(columnNames);
			int maxReturnRow = resultLimit < USER_SHOW_ROW_MAXIMUM_NUMBER ? resultLimit
					: USER_SHOW_ROW_MAXIMUM_NUMBER;
			int currentRow = 1;
			while (rs.next()) {
				List<String> oneRowData = new ArrayList<String>();
				if (currentRow > FILE_STORE_ROW_MAXIMUM_NUMBER) {
					break;
				} else {
					for (int i = 1; i <= columnCount; i++) {
						if (currentRow <= maxReturnRow) {
							oneRowData.add(rs.getString(i));
						}
					}
				}
				currentRow++;
				res.addRow(oneRowData);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("db:" + database + " hql:" + hql, e);
		}
		return res;
	};
}
