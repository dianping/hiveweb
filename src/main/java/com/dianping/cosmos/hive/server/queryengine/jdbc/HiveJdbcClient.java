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

import com.dianping.cosmos.hive.client.bo.FieldSchemaBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.shared.util.ReflectUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

@Service
public class HiveJdbcClient {
	private static final Log logger = LogFactory.getLog(HiveJdbcClient.class);

	private static final String HIVE_JDBC_DRIVER_CLASS = "org.apache.hadoop.hive.jdbc.HiveDriver";
	
	private static final int USER_SHOW_ROW_MAXIMUM_COUNT = 500;
	private static final char FIELD_DELIMITED = '\t';
	private static final char LINE_DELIMITED = '\n';

	private static final Cache<String, Connection> connCache = CacheBuilder
			.newBuilder().concurrencyLevel(4).maximumSize(100000)
			.expireAfterWrite(24, TimeUnit.HOURS)
			.removalListener(new RemovalListener<String, Connection>() {

				@Override
				public void onRemoval(RemovalNotification<String, Connection> rn) {
					logger.info(("tokenid:" + rn.getKey() + " Connection"
							+ rn.getValue() + " was removed from connCache"));
				}

			}).build();

	private static final Cache<String, UserGroupInformation> ugiCache = CacheBuilder
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
			Class.forName(HIVE_JDBC_DRIVER_CLASS);
		} catch (ClassNotFoundException cfe) {
			logger.error("Hive Driver Not Found " + HIVE_JDBC_DRIVER_CLASS, cfe);
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
		Statement stmt = null;
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
				logger.error(e);
			}
		}
		return dbs;
	}

	public List<String> getTables(String tokenid, String database) {
		List<String> tables = new ArrayList<String>();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		Statement stmt = null;
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
				logger.error(e);
			}
		}
		return tables;
	}

	public List<FieldSchemaBo> getTableSchema(String tokenid, String database,
			String table) {
		List<FieldSchemaBo> fieldSchemaList = new ArrayList<FieldSchemaBo>();
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.execute("use " + database);
			ResultSet rs = stmt.executeQuery("desc " + table);
			while (rs.next()) {
				FieldSchemaBo fieldSchema = new FieldSchemaBo();
				fieldSchema.setFieldName(rs.getString(1));
				fieldSchema.setFieldType(rs.getString(2));
				fieldSchema.setFieldComment(rs.getString(3));
				fieldSchemaList.add(fieldSchema);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("field schema of table " + table + " is " + StringUtils.join(fieldSchemaList, ";"));
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error("get table schema failed", e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return fieldSchemaList;
	}

	public String getTableSchemaDetail(String tokenid, String database,
			String table) {
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		StringBuilder sb = new StringBuilder(1000);
		Statement stmt = null;
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
						if (i < columnCount) {
							sb.append(rs.getString(i)).append(FIELD_DELIMITED);
						} else {
							sb.append(rs.getString(i));
						}
					}
				}
				sb.append(LINE_DELIMITED);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			logger.error(
					String.format(
							"get table schema detail failed , tokenid:%s, database:%s, table:%s",
							tokenid, database, table), e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		return sb.toString();
	}

	@SuppressWarnings({ "unchecked" })
	public String getQueryPlan(String tokenid, String hql, String database) {
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		StringBuilder sb = new StringBuilder(1000);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery("use " + database);
			stmt.setFetchSize(Integer.MAX_VALUE);
			ResultSet rs = stmt.executeQuery("explain extended " + hql);
			if (rs.next()) {
				Object value = ReflectUtils.getFieldValue(rs, "fetchedRows");
				List<String> fetchedRows = (List<String>) value;
				if (fetchedRows == null || fetchedRows.size() == 0) {
					sb.append("something wrong with hive query");
				} else {
					int fetchedRowsSize = fetchedRows.size();
					for (int i = 0; i < fetchedRowsSize; i++) {
						if (i < fetchedRowsSize - 1) {
							sb.append(fetchedRows.get(i)).append(LINE_DELIMITED);
						}else {
							sb.append(fetchedRows.get(i));
						}
					}
				}
			} else {
				logger.error("execute 'explain extended hql' failed, hql:" + hql);
			}
			rs.close();
			stmt.close();
		} catch (Exception e) {
			sb.append(e.getMessage());
			logger.error("getQueryPlan failed, dbname:" + database + " hql:"
					+ hql, e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		
		if (logger.isDebugEnabled()){
			logger.debug("dbname:" + database + ", the query plan of hql:" + hql
					+ " is " + sb.toString());
		}
		return sb.toString();
	}

	public HiveQueryOutputBo getQueryResult(String tokenid, String username,
			String database, String hql, int resultLimit, Boolean isStoreFile, String resultLocation, 
			long timestamp) {
		HiveQueryOutputBo hqo = new HiveQueryOutputBo(); 
		UserGroupInformation ugi = ugiCache.getIfPresent(tokenid);
		Connection conn = getConnection(ugi);
		Statement stmt = null;
		try {
			stmt = conn.createStatement();
			stmt.executeQuery("use " + database);
			ResultSet rs = stmt.executeQuery(hql);
			ResultSetMetaData rsm = rs.getMetaData();
			int columnCount = rsm.getColumnCount();
			String[] columnNames = new String[columnCount];

			StringBuilder sb = new StringBuilder(200);
			for (int i = 1; i <= columnCount; i++) {
				sb.append(rsm.getColumnName(i));
				if (i < columnCount) {
					sb.append(FIELD_DELIMITED);
				}else {
					sb.append(LINE_DELIMITED);
				}
			}
			if (logger.isDebugEnabled()) {
				logger.debug("resultset meta data is:" + sb.toString());
			}
			hqo.setFieldSchema(columnNames);
			int maxShowRowCount = resultLimit < USER_SHOW_ROW_MAXIMUM_COUNT ? resultLimit
					: USER_SHOW_ROW_MAXIMUM_COUNT;
			int currentRow = 0;

			BufferedWriter bw = null;
			if (isStoreFile && !"".equals(resultLocation)) { 
				bw = DataFileStore.openOutputStream(resultLocation);
				// Write Resultset field names to the first line of result file
				bw.write(sb.toString());
			}
			logger.info("isStoreFile:" + isStoreFile + " storeFilePath:"
					+ resultLocation);
			hqo.setResultFileAbsolutePath(resultLocation);

			while (rs.next()
					&& currentRow < DataFileStore.FILE_STORE_LINE_LIMIT) {
				if (currentRow < maxShowRowCount) {
					String[] oneRowData = new String[columnCount];
					for (int i = 1; i <= columnCount; i++) {
						String value = rs.getString(i) == null ? "" : rs
								.getString(i);
						oneRowData[i - 1] = value;
						if (isStoreFile) {
							bw.write(value);
							if (i < columnCount) {
								bw.write(FIELD_DELIMITED);
							} else {
								bw.write(LINE_DELIMITED);
							}
						}
					}
					hqo.addOneRow(oneRowData);
				} else if (isStoreFile) {
					for (int i = 1; i <= columnCount; i++) {
						bw.write(rs.getString(i));
						if (i < columnCount) {
							bw.write(FIELD_DELIMITED);
						} else {
							bw.write(LINE_DELIMITED);
						}
					}
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
			hqo.setErrorMsg(e.getMessage());
			logger.error("get query result failed, db:" + database + " hql:"
					+ hql, e);
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				logger.error(e);
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
