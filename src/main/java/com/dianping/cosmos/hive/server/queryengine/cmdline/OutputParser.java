package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;

public class OutputParser {
	private static final Log logger = LogFactory.getLog(OutputParser.class);
	
	private static final char DELIMITER = '\t';
	private static OutputParser s_instance = new OutputParser();
	
	private OutputParser(){}
		
	public static OutputParser getInstance(){
		return s_instance;
	}
	
	public HiveQueryOutputBo parse(InputStream is, int limit) throws IOException{
		HiveQueryOutputBo result = new HiveQueryOutputBo();
		LineIterator it = IOUtils.lineIterator(is, BasicUtils.ENCODING);
		try{
			//the first line is column names
			if (it.hasNext()) {
				result.setFieldSchema(parseOneLine(it.nextLine()));
			}
			int lineNum = 0;
			while(it.hasNext() && lineNum < limit){
				String data = it.nextLine();
				result.addOneRow(parseOneLine(data));
				lineNum++;
			}
			logger.info("data row number:" + result.getData().size());
		}finally{
			it.close();
		}
		return result;
	}

	private String[] parseOneLine(String line){
		String[] fields = StringUtils.splitPreserveAllTokens(line, DELIMITER);
		if(fields == null || fields.length <= 0){
			return null;
		}
		return fields;
	}
}
