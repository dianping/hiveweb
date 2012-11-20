package com.dianping.cosmos.hive.server.queryengine;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;

public class HiveQueryInput {

	private String hql;
	private HiveQueryInputBo bo;

	public HiveQueryInput(HiveQueryInputBo bo) {
		String db = bo.getDatabase();
		hql = "use " + db + ";" + bo.getHql();
		this.bo = bo;
	}

	public String getHql() {
		return hql;
	}
	
	public String getDatabase() {
		return bo.getDatabase();
	}
	
	public String getOriginalHql(){
		return bo.getHql();
	}

	public String getTokenid() {
		return bo.getTokenid();
	}

	public String getUsername() {
		return bo.getUsername();
	}

	public long getTimestamp() {
		return bo.getTimestamp();
	}

	public boolean isStoreResult() {
		return bo.isStoreResult();
	}

	public int getResultLimit() {
		return bo.getResultLimit();
	}
}
