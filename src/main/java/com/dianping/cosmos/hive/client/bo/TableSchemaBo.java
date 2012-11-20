package com.dianping.cosmos.hive.client.bo;

import com.google.gwt.user.client.rpc.IsSerializable;

public class TableSchemaBo implements IsSerializable {
	private String fieldName;
	private String fieldType;
	private String fieldComment;
	
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public String getFieldComment() {
		return fieldComment;
	}
	public void setFieldComment(String fieldComment) {
		this.fieldComment = fieldComment;
	}
	
	@Override
	public String toString() {
		return "TableSchemaBo [fieldName=" + fieldName + ", fieldType="
				+ fieldType + ", fieldComment=" + fieldComment + "]";
	}

}
