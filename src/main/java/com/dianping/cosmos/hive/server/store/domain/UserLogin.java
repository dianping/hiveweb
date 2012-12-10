package com.dianping.cosmos.hive.server.store.domain;

import java.io.Serializable;
import java.util.Date;

public class UserLogin implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String username;
	private Date logintime;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getLogintime() {
		return logintime;
	}
	public void setLogintime(Date logintime) {
		this.logintime = logintime;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((logintime == null) ? 0 : logintime.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserLogin other = (UserLogin) obj;
		if (logintime == null) {
			if (other.logintime != null)
				return false;
		} else if (!logintime.equals(other.logintime))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "UserLogin [username=" + username + ", logintime=" + logintime
				+ "]";
	}
}
