package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.cmdline.BasicUtils;
import com.dianping.cosmos.hive.server.queryengine.cmdline.OutputParser;

class StreamToFileHandler extends BaseStreamHandler{
	
	private static final Log s_logger = LogFactory.getLog(StreamToFileHandler.class);
	
	private static final int DEFAULT_BUFFER = 1024 * 50;
	private static final int MAX_OUT_FILE_SIZE = 1024 * 1024 * 1024;

	private String outFileLocation;
	private int maxFileSize;
	private boolean gzip;

	public StreamToFileHandler(String outFileLocation, int maxFileSize, boolean gzip){
		this.outFileLocation = outFileLocation;
		this.maxFileSize = maxFileSize;
		this.gzip = gzip;
	}
	
	public StreamToFileHandler(String outFileLocation, boolean gzip){
		this(outFileLocation, MAX_OUT_FILE_SIZE, gzip);
	}

	public void run() {
		if(is == null || outFileLocation == null)
			return;

		final byte[] buf = new byte[DEFAULT_BUFFER];
		int length;
		int total = 0;
		try {
			while ((length = is.read(buf)) > 0 && total < maxFileSize) {
				BasicUtils.writeByteArrayToFile(new File(outFileLocation), Arrays.copyOfRange(buf, 0, length), true, gzip);
				total += length;
			}
		} catch (IOException e) {
			s_logger.error("Exception occurs in writing to file", e);
		} 
	}

	public HiveQueryOutput getResult(){
		try {
			return OutputParser.getInstance().parse(
					BasicUtils.openInputStream(new File(outFileLocation), gzip), limit);
		} catch (IOException e) {
			s_logger.error("Exception occurs in parsing output:", e);
			return null;
		}
	}

}
