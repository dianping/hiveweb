package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.IStreamHandler;
import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.StreamHandlerFactory;

public class HiveCmdLineQueryEngine implements IQueryEngine{
	
	private static final Log s_logger = LogFactory.getLog(HiveCmdLineQueryEngine.class);
	private static final String BASE_DIR = "/data/hqe/";
	private static final String RESULT_BASE_DIR = BASE_DIR + "result";
	private static final String STATUS_BASE_DIR = BASE_DIR + "status";
	private static final String HIVE_CMD = "/usr/local/hadoop/hive-release/bin/hive";
	
	private static Set<String> s_hiveConfigSet = new HashSet<String>();

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		//preparation
		if(input == null){
			s_logger.error("HiveQueryInput is empty!");
			return null;
		}
		String username = input.getUsername();
		String hiveCmd = input.getHql();
		long timestamp = input.getTimestamp();
		boolean storeResultToFile = input.isStoreResult();
		int limit = input.getResultLimit();
		if(StringUtils.isEmpty(username) || StringUtils.isEmpty(username.trim())){
			s_logger.error("Input username is empty!");
			return null;
		}
		if(StringUtils.isEmpty(hiveCmd) || StringUtils.isEmpty(hiveCmd.trim())){
			s_logger.error("Input hqls is empty!");
			return null;
		}
		String key = getKey(username, timestamp);
		String statusLocation = getStatusFileLocation(username, timestamp);
		String resultLocation = getResultLocation(username, timestamp);
		boolean returnResult = false;
		
		hiveCmd = hiveCmd.toLowerCase().trim();
		if(hiveCmd.startsWith("select"))
			returnResult = true;

		IStreamHandler statusHandler = StreamHandlerFactory.createStatusHandler(statusLocation);
		IStreamHandler resultHandler = null;

		if(returnResult){
			if(storeResultToFile){
				resultHandler = StreamHandlerFactory.createFileResultHandler(resultLocation);
			}
			else{
				resultHandler = StreamHandlerFactory.createMemResultHandler();	
				hiveCmd = joinString(hiveCmd, " limit ", ""+limit);
			}
			resultHandler.setLimit(limit);
		}
		
		if(!storeResultToFile && !hiveCmd.contains(" limit ")){
			hiveCmd = joinString(hiveCmd, " limit ", ""+limit);
		}
		if(!hiveCmd.endsWith(";")){
			hiveCmd += ";";
		}
		String cmd = joinString("sudo -u ", username," sh -c \"source /etc/profile;" ,HIVE_CMD, " -e \\\"", hiveCmd, "\\\"");
		//add hiveconf
		for(String hiveConf: s_hiveConfigSet){
			cmd = joinString(cmd, " -hiveconf ", hiveConf, " \"");
		}
		//execute cmd
		int exitCode = -1;
		try {
			exitCode = ShellCmdExecutor.getInstance().execute(cmd, key, resultHandler, statusHandler);
		} catch (ShellCmdExecException e) {
			s_logger.error("Exceptions occurs in executing hive command!", e);
			return null;
		}
		if(exitCode != 0){
			s_logger.error("Hive Command is NOT executed successfully! The exit code of hive command is " + exitCode);
			return null;
		}

		return resultHandler.getResult();
	}

	@Override
	public String getQueryStatus(String username, long timestamp) {
		String statusFileLocation = getStatusFileLocation(username, timestamp);
		try {
			return FileUtils.readFileToString(new File(statusFileLocation), BasicUtils.ENCODING);
		} catch (IOException e) {
			s_logger.error("Exception occurs in reading status file: " + statusFileLocation, e);
			return null;
		}
	}

	@Override
	public void stopQuery(String username, long timestamp) {
		//stop process
		String key = getKey(username, timestamp);
		try {
			ShellCmdExecutor.getInstance().stopRunningTask(key);
		} catch (Exception e) {
			s_logger.error("The running query for key: " + key + "is NOT found!", e);
			return;
		}
		//stop submitted hadoop job
		System.out.println("start to kill hadoop job");
		try {
			LineIterator it = BasicUtils.lineIterator(
					new File(getStatusFileLocation(username, timestamp)), BasicUtils.ENCODING, false);
			while(it.hasNext()){
				String line = it.next().toString();
				if(StringUtils.isEmpty(line) || StringUtils.isEmpty(line.trim()))
					continue;
				line = line.trim();
				if(line.startsWith("Kill Command")){
					System.out.println(line);
					//get kill command
					String killCommand = line.substring("Kill Command =".length());
					killCommand = joinString("sudo -u ", username," sh -c \"source /etc/profile;", killCommand, " \"");
					System.out.println("Kill Command:" + killCommand);
					ShellCmdExecutor.getInstance().execute(killCommand, null, null, null);
				}
			}
		} catch (Exception e) {
			s_logger.error("Exception Occurs in stopping running hadoop task!", e);
			return;
		}	
	}
	
	private String getKey(String username, long timestamp){
		return joinString(username, "_", ""+timestamp);
	}
	
	private String getResultLocation(String username, long timestamp){
		String resultFilename = joinString(""+timestamp, ".csv.gz");
		return joinLocation(RESULT_BASE_DIR, username, resultFilename);
	}
	
	private String getStatusFileLocation(String username, long timestamp){
		String statusFilename = joinString(username, "_", ""+timestamp, ".stat");
		return joinLocation(STATUS_BASE_DIR, statusFilename);
	}

	private String joinString(String... strings){
		return BasicUtils.joinString(strings);
	}

	private String joinLocation(String... locationParts){
		return BasicUtils.joinLocation(locationParts);
	}
}
