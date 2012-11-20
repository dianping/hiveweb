package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.InputStream;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;

public interface IStreamHandler extends Runnable{
	
	public void setInputStream(InputStream is);
	
	public void setLimit(int limit);
	
	public HiveQueryOutput getResult();
}
