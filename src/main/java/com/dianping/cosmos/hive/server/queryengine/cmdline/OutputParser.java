package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;

public class OutputParser {
	
	private static final String FIELD_SEP = "\t";
	private static OutputParser s_instance = new OutputParser();
	
	private OutputParser(){}
		
	public static OutputParser getInstance(){
		return s_instance;
	}
	
	public HiveQueryOutput parse(InputStream is, int limit) throws IOException{
		HiveQueryOutput result = new HiveQueryOutput();
		LineIterator it = IOUtils.lineIterator(is, BasicUtils.ENCODING);
		try{
			//the first line is column names
			result.setTitleList(parseOneLine(it.next().toString()));
			int lineNum = 0;
			while(it.hasNext() && lineNum < limit){
				result.addRow(parseOneLine(it.next().toString()));
				lineNum ++;
			}
		}finally{
			it.close();
		}
		return result;
	}

	private List<String> parseOneLine(String line){
		List<String> r = new ArrayList<String>();
		String[] fields = line.split(FIELD_SEP);
		if(fields == null || fields.length <= 0)
			return r;
		for(String field: fields){
			r.add(field == null? "" : field.trim());
		}
		return r;
	}
}
