package com.dianping.cosmos.hive.client.bo;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LoginTokenBo implements IsSerializable{
	private String tokenid;
	private Date addtime;
	
	public String getTokenid() {
		return tokenid;
	}
	public void setTokenid(String tokenid) {
		this.tokenid = tokenid;
	}
	public Date getAddtime() {
		return addtime;
	}
	public void setAddtime(Date addtime) {
		this.addtime = addtime;
	}
	
	@Override
	public String toString() {
		return "LoginTokenBo [tokenid=" + tokenid + ", addtime=" + addtime
				+ "]";
	}

}
