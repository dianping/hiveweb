package com.dianping.cosmos.hive.server.queryengine.jdbc;

import java.io.BufferedWriter;
import java.security.PrivilegedAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5Login;
import org.apache.hadoop.security.UserGroupInformation;
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

	private static final int USER_SHOW_ROW_MAXIMUM_COUNT = 300;

	private static String driverName = "org.apache.hadoop.hive.jdbc.HiveDriver";

	private static Cache<String, Connection> connCache = CacheBuilder
			.newBuilder().concurrencyLevel(4).maximumSize(100000)
			.expireAfterWrite(24, TimeUnit.HOURS)
			.removalListener(new RemovalListener<String, Connection>() {

				@Override
				public void onRemoval(RemovalNotification<String, Connection> rn) {
					logger.info(("tokenid:" + rn.getKey() + " Connection"
							+ rn.getValue() + " was removed from connCache"));
				}

			}).build();

	private static Cache<String, UserGroupInformation> ugiCache = CacheBuilder
			.newBuilder()
			.concurrencyLevel(4)
			.maximumSize(100000)
			.expireAfterWrite(24, TimeUnit.HOURS)
			.removalListener(
					new RemovalListener<String, UserGroupInformation>() {

						@Override
						public void onRemoval(
								RemovalNotification<String, UserGroupInformation> rn) {
							logger.info(("tokenid:" + rn.getKey()
									+ " UserGroupInformation:" + rn.getValue() + " was removed from connCache"));
						}

					}).build();

	static {
		try {
			Class.forName(driverName);
		} catch (ClassNotFoundException cfe) {
			logger.error("Hive Driver Not Found " + driverName, cfe);
		}
	}

	public static void putTokenConn(String tokenid, Connection conn) {
		connCache.put(tokenid, conn);
	}

	public static Connection getTokenConn(String tokenid) {
		return connCache.getIfPresent(tokenid);
	}

	public static UserGroupInformation getUgiCache(String tokenid) {
		return ugiCache.getIfPresent(tokenid);
	}

	public static void putUgiCache(String tokenid, UserGroupInformation ugi) {
		ugiCache.put(tokenid, ugi);
	}

	public static void removeConnectionByTokenid(String tokenid) {
		connCache.invalidate(tokenid);
	}

	public static void removeUgiByTokenid(String tokenid) {
		ugiCache.invalidate(tokenid);
	}

	public List<String> getDatabases(String tokenid) {
		List<String> dbs = new ArrayList<String>();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("show databases");
			while (rs.next()) {
				if (rs.getString(1).equalsIgnoreCase("default")) {
					dbs.add(0, rs.getString(1));
				} else {
					dbs.add(rs.getString(1));
				}
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("getDatabases:" + e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return dbs;
	}

	public List<String> getTables(String tokenid, String database) {
		List<String> tables = new ArrayList<String>();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
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
			logger.error("getTables failed :" + e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tables;
	}

	public List<TableSchemaBo> getTableSchema(String tokenid, String database,
			String table) {
		List<TableSchemaBo> tsbs = new ArrayList<TableSchemaBo>();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
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

				if (logger.isDebugEnabled()) {
					logger.debug("getTableSchema " + tsb);
				}
				tsbs.add(tsb);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("getTableSchema failed", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tsbs;
	}

	public String getTableSchemaDetail(String tokenid, String database,
			String table) {
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		StringBuilder sb = new StringBuilder(1000);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.execute("use " + database);
			ResultSet rs = stmt.executeQuery("desc formatted " + table);
			ResultSetMetaData rsm = rs.getMetaData();
			int columnCount = rsm.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= columnCount; i++) {
					if (StringUtils.isBlank(rs.getString(i))) {
						continue;
					} else {
						sb.append(rs.getString(i)).append('\t');
					}
				}
				sb.append('\n');
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error(
					String.format(
							"getTableSchemaDetail failed , tokenid:%s, database:%s, table:%s",
							tokenid, database, table), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked" })
	public String getQueryPlan(String tokenid, String hql, String database) {
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		StringBuilder sb = new StringBuilder(1000);
		Statement stmt;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery("use " + database);
			stmt.setFetchSize(Integer.MAX_VALUE);
			ResultSet rs = stmt.executeQuery("explain " + hql);
			if (rs.next()) {
				Object value = ReflectUtils.getFieldValue(rs, "fetchedRows");
				List<String> fetchedRows = (List<String>) value;
				if (fetchedRows == null || fetchedRows.size() == 0) {
					sb.append("something wrong with hive query");
				} else {
					for (String row : fetchedRows) {
						sb.append(row).append("\n");
					}
				}
			} else {
				logger.error("execute 'explain hql' failed, hql:" + hql);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			sb.append(e.toString());
			logger.error("getQueryPlan failed, dbname:" + database + " hql:"
					+ hql, e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (logger.isDebugEnabled()){
			logger.debug("dbname:" + database + " query plan for hql:" + hql
					+ " is " + sb.toString());
		}
		return sb.toString();
	}

	public HiveQueryOutput getQueryResult(String tokenid, String username,
			String database, String hql, int resultLimit, Boolean isStoreFile,
			long timestamp) {
		HiveQueryOutput hqo = new HiveQueryOutput();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
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

			hqo.setTitleList(columnNames);
			int maxShowRowCount = resultLimit < USER_SHOW_ROW_MAXIMUM_COUNT ? resultLimit
					: USER_SHOW_ROW_MAXIMUM_COUNT;
			int currentRow = 0;

			String storeFilePath = "";
			BufferedWriter bw = null;
			if (isStoreFile) {
				storeFilePath = DataFileStore.getStoreFilePath(tokenid,
						username, database, hql, timestamp);
				bw = DataFileStore.openOutputStream(storeFilePath);
			}
			logger.info("isStoreFile:" + isStoreFile + " storeFilePath:"
					+ storeFilePath);

			if (!StringUtils.isBlank(storeFilePath)) {
				hqo.setStoreFileLocation(storeFilePath);
			}

			while (rs.next()
					&& currentRow < DataFileStore.FILE_STORE_LINE_LIMIT) {
				if (currentRow < maxShowRowCount) {
					List<String> oneRowData = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++) {
						String value = rs.getString(i) == null ? "" : rs
								.getString(i);
						oneRowData.add(value);
						if (isStoreFile) {
							bw.write(value);
							if (i < columnCount)
								bw.write('\t');
						}
					}
					hqo.addRow(oneRowData);

					if (isStoreFile) {
						bw.write('\n');
					}
				} else if (isStoreFile) {
					for (int i = 1; i <= columnCount; i++) {
						bw.write(rs.getString(i));
						if (i < columnCount)
							bw.write('\t');
					}
					bw.write('\n');
				} else {
					break;
				}
				currentRow++;
			}
			if (isStoreFile && bw != null) {
				bw.flush();
				IOUtils.closeQuietly(bw);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			hqo.setErrorMessage(e.toString());
			logger.error("getQueryResult failed, db:" + database + " hql:"
					+ hql, e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return hqo;
	};

	public static Connection getConnection(final UserGroupInformation ugi) {
		Connection conn = null;
		if (ugi != null){
			conn = ugi.doAs(new PrivilegedAction<Connection>() {
				@Override
				public Connection run() {
					Connection c = null;
					try {
						if (logger.isDebugEnabled()) {
							logger.debug("start get connection ugi username:" + ugi.getUserName()) ;
						}
						
						c = DriverManager.getConnection(
								Krb5Login.HIVE_CONNECTION_URL, "", "");
						
						if (logger.isDebugEnabled()) {
							logger.debug("through get connection ugi username:" + ugi.getUserName()) ;
						}
					} catch (Exception e) {
						logger.error("get connection failed：" + e.getMessage());
					}
					return c;
				}
			});
		}
		return conn;
	}
}
