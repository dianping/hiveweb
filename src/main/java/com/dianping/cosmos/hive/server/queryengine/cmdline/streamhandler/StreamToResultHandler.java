package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.server.queryengine.cmdline.OutputParser;

class StreamToResultHandler extends BaseStreamHandler{
	
	private static final Log s_logger = LogFactory.getLog(StreamToResultHandler.class);

	private HiveQueryOutputBo result;

	public void run() {
		if(is == null)
			return;

		try {
			result = OutputParser.getInstance().parse(is, showLimit);
		} catch (IOException e) {
			s_logger.error("Exception occurs in parsing output:", e);
		}
	}

	public HiveQueryOutputBo getResult() {
		return result;
	}

}
