package com.dianping.cosmos.hive.client.bo;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class QueryHistoryBo implements IsSerializable {
	private String username;
	private String hql;
	private Date addtime;
	private String filename;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
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
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	@Override
	public String toString() {
		return "QueryHistoryBo [username=" + username + ", hql=" + hql
				+ ", addtime=" + addtime + ", filename=" + filename + "]";
	}

}
