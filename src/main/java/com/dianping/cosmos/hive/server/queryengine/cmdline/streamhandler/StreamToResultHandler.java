package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.cmdline.OutputParser;

class StreamToResultHandler extends BaseStreamHandler{
	
	private static final Log s_logger = LogFactory.getLog(StreamToResultHandler.class);

	private HiveQueryOutput result;

	public void run() {
		if(is == null)
			return;

		try {
			result = OutputParser.getInstance().parse(is, limit);
		} catch (IOException e) {
			s_logger.error("Exception occurs in parsing output:", e);
		}

	}

	public HiveQueryOutput getResult() {
		return result;
	}

}
