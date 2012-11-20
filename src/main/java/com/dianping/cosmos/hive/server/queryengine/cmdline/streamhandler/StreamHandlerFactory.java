package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

public class StreamHandlerFactory {
	
	public static IStreamHandler createStatusHandler(String statusFileLocation){
		return new StreamToFileHandler(statusFileLocation, false);
	}
	
	public static IStreamHandler createFileResultHandler(String outFileLocation){
		return new StreamToFileHandler(outFileLocation, true);
	}
	
	public static IStreamHandler createMemResultHandler(){
		return new StreamToResultHandler();
	}

}
