package com.dianping.cosmos.hive.client.bo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class HiveQueryInputBo implements IsSerializable {
	private String hql;
	private String database;
	private String username;
	private long timestamp;
	private boolean storeResult;
	private int resultLimit;
	private String tokenid;
	
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
	public String getDatabase() {
		return database;
	}
	public void setDatabase(String database) {
		this.database = database;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public boolean isStoreResult() {
		return storeResult;
	}
	public void setStoreResult(boolean storeResult) {
		this.storeResult = storeResult;
	}
	public int getResultLimit() {
		return resultLimit;
	}
	public void setResultLimit(int resultLimit) {
		this.resultLimit = resultLimit;
	}
	public String getTokenid() {
		return tokenid;
	}
	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}
	
	@Override
	public String toString() {
		return "HiveQueryInputBo [hql=" + hql + ", database=" + database
				+ ", username=" + username + ", timestamp=" + timestamp
				+ ", storeResult=" + storeResult + ", resultLimit="
				+ resultLimit + ", tokenid=" + tokenid + "]";
	}
}
