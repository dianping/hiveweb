package com.dianping.cosmos.hive.server.queryengine;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;

public class HiveQueryOutput{
	
	private List<String> titleList;
	
	private List<List<String>> rowList = new ArrayList<List<String>>();

	public List<String> getTitleList() {
		return titleList;
	}

	public void setTitleList(List<String> titleList) {
		this.titleList = new ArrayList<String>(titleList);
	}
	
	public void addRow(List<String> row){
		rowList.add(row);
	}

	public List<List<String>> getRowList() {
		return rowList;
	}
	
	public HiveQueryOutputBo toHiveQueryOutputBo(){
		HiveQueryOutputBo bo = new HiveQueryOutputBo();
		
		bo.setFieldSchema(titleList.toArray(new String[titleList.size()]));
		List<String[]> data = new ArrayList<String[]>();
		
		int rowCount = rowList.size();
		for (int i = 0; i < rowCount; i++){
			int columnCount = rowList.get(i).size();
			data.add(rowList.get(i).toArray(new String[columnCount]));
		}
		bo.setData(data);
		return bo;
	}

	@Override
	public String toString() {
		return "HiveQueryOutput [titleList=" + titleList + ", rowList="
				+ rowList + "]";
	}
}
