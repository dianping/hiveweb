package com.dianping.cosmos.hive.client.bo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QueryFavoriteBo implements IsSerializable {
	private String queryName;
	private String hql;
	
	public String getQueryName() {
		return queryName;
	}
	public String getHql() {
		return hql;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
	
	@Override
	public String toString() {
		return "QueryFavoriteBo [queryName=" + queryName + ", hql=" + hql + "]";
	}
}
