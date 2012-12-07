package com.dianping.cosmos.hive.server.queryengine;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;

public class HiveQueryOutput{
	
	private List<String> titleList;
	
	private List<List<String>> rowList = new ArrayList<List<String>>();
	
	private String storeFileLocation;
	
	private String errorMessage;

	public List<String> getTitleList() {
		return titleList;
	}
	
	public String getStoreFileLocation(){
		return this.storeFileLocation;
	}
	
	public void setStoreFileLocation(String fileLocation){
		this.storeFileLocation = fileLocation;
	}

	public void setTitleList(List<String> titleList) {
		this.titleList = new ArrayList<String>(titleList);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public void addRow(List<String> row){
		rowList.add(row);
	}

	public List<List<String>> getRowList() {
		return rowList;
	}
	
	public HiveQueryOutputBo toHiveQueryOutputBo(){
		HiveQueryOutputBo bo = new HiveQueryOutputBo();
		
		String[] fieldsSchema = new String[]{};
		if (titleList != null && titleList.size() > 0){
			fieldsSchema = titleList.toArray(new String[titleList.size()]);
		}
		bo.setFieldSchema(fieldsSchema);
		List<String[]> data = new ArrayList<String[]>();
		
		int rowCount = 0;
		if (rowList != null){
			rowCount = rowList.size();
		}
		
		for (int i = 0; i < rowCount; i++){
			int columnCount = rowList.get(i).size();
			data.add(rowList.get(i).toArray(new String[columnCount]));
		}
		bo.setData(data);
		bo.setErrorMsg(errorMessage);
		return bo;
	}

	@Override
	public String toString() {
		return "HiveQueryOutput [titleList=" + titleList + ", rowList="
				+ rowList + "]";
	}
}
