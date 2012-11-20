package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.InputStream;

abstract class BaseStreamHandler implements IStreamHandler{
	
	protected InputStream is;
	
	protected int limit;

	public void setInputStream(InputStream is){
		this.is = is;
	}
	
	public void setLimit(int limit){
		this.limit = limit;
	}

}
