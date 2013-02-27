package com.dianping.cosmos.hive.client.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class HiveQueryOutputBo implements IsSerializable {
	private long execTime;
	private String[] fieldSchema;
	private List<String[]> data;
	private String errorMsg;
	private String resultFileAbsolutePath;
	private Boolean success;

	public HiveQueryOutputBo() {
		data = new ArrayList<String[]>();
		errorMsg = "";
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

	public boolean addOneRow(String[] row) {
		return data.add(row);
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

	public String getResultFileAbsolutePath() {
		return resultFileAbsolutePath;
	}

	public void setResultFileAbsolutePath(String resultFileAbsolutePath) {
		this.resultFileAbsolutePath = resultFileAbsolutePath;
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "HiveQueryOutputBo [execTime=" + execTime + ", fieldSchema="
				+ Arrays.toString(fieldSchema) + ", data=" + data
				+ ", errorMsg=" + errorMsg + ", resultFileAbsolutePath="
				+ resultFileAbsolutePath + ", success=" + success + "]";
	}
}
