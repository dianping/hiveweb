package com.dianping.cosmos.hive.client.bo;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class HiveQueryOutputBo implements IsSerializable {
	private long execTime;
	private String[] fieldSchema;
	private List<String[]> data;
	private String errorMsg;
	
	public HiveQueryOutputBo(){
	}
	
	public long getExecTime() {
		return execTime;
	}

	public void setExecTime(long execTime) {
		this.execTime = execTime;
	}

	public String[] getFieldSchema() {
		return fieldSchema;
	}

	public void setFieldSchema(String[] fieldSchema) {
		this.fieldSchema = fieldSchema;
	}

	public List<String[]> getData() {
		return data;
	}

	public void setData(List<String[]> data) {
		this.data = data;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	@Override
	public String toString() {
		return "HiveQueryOutput [execTime=" + execTime + ", fieldSchema="
				+ Arrays.toString(fieldSchema) + ", data=" + data
				+ ", errorMsg=" + errorMsg + "]";
	}

}
