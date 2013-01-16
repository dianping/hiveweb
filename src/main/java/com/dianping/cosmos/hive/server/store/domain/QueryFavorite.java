package com.dianping.cosmos.hive.server.store.domain;

import java.io.Serializable;
import java.util.Date;

public class QueryFavorite implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String queryName;
	private String hql;
	private Date addtime;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getQueryName() {
		return queryName;
	}
	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}
	public String getHql() {
		return hql;
	}
	public void setHql(String hql) {
		this.hql = hql;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
	
	@Override
	public String toString() {
		return "QueryFavorite [username=" + username + ", queryName="
				+ queryName + ", hql=" + hql + ", addtime=" + addtime + "]";
	}

}
