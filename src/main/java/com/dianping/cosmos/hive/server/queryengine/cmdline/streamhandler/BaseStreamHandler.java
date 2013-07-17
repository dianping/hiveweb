package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.InputStream;

abstract class BaseStreamHandler implements IStreamHandler{
	
	protected InputStream is;
	
	protected Process proc;
	
	protected int showLimit;
	
	protected volatile Boolean killStatus;

	public void setInputStream(InputStream is){
		this.is = is;
	}
	
	public void setShowLimit(int limit){
		this.showLimit = limit;
	}
	
	public void setExecuteProcess(Process proc){
		this.proc = proc;
	}
	
	public Boolean getProcessKillStatus() {
		return killStatus;
	}
	
	public void setProcessKillStatus(Boolean killStatus) {
		this.killStatus = killStatus;
	}

}
