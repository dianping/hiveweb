package com.dianping.cosmos.hive.client.bo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ResultStatusBo implements IsSerializable {
	private boolean success;
	private String message;
	
	public ResultStatusBo(){
	}
	
	public ResultStatusBo(boolean success, String message){
		this.success = success;
		this.message = message;
	}
	
	public boolean isSuccess() {
		return success;
	}
	public String getMessage() {
		return message;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "ResultStatusBo [success=" + success + ", message=" + message
				+ "]";
	}
}
