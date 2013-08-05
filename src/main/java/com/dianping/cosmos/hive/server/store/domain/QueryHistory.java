package com.dianping.cosmos.hive.server.store.domain;

import java.io.Serializable;
import java.util.Date;

public class QueryHistory implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private String hql;
	private Date addtime;
	private String filename;
	private String mode;
	private long exectime;
	
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
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getMode() {
		return mode;
	}
	public void setExectime(long exectime) {
		this.exectime = exectime;
	}
	public long getExectime() {
		return exectime;
	}
	
	@Override
	public String toString() {
		return "QueryHistory [username=" + username + ", hql=" + hql
				+ ", addtime=" + addtime + ", filename=" + filename + "]";
	}
}
