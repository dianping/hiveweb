package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.client.bo.HiveQueryInputBo;
import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryInput;
import com.dianping.cosmos.hive.server.queryengine.HiveQueryOutput;
import com.dianping.cosmos.hive.server.queryengine.IQueryEngine;
import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.IStreamHandler;
import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.StreamHandlerFactory;
import com.dianping.cosmos.hive.server.queryengine.jdbc.DataFileStore;

public class HiveCmdLineQueryEngine implements IQueryEngine {

	private static final Log logger = LogFactory
			.getLog(HiveCmdLineQueryEngine.class);

	private static final String KILL_COMMAND_PREFIX = "Kill Command =";
	private static final int KILL_COMMAND_PREFIX_LENGTH = KILL_COMMAND_PREFIX
			.length();

	@Override
	public HiveQueryOutputBo getQueryResult(HiveQueryInputBo input) {
		// preparation
		if (input == null) {
			logger.error("HiveQueryInput is empty!");
			return null;
		}
		String username = input.getUsername();
		String hiveCmd = input.getHql().trim();
		if (StringUtils.isEmpty(hiveCmd)) {
			logger.error("Input hqls is empty!");
			return null;
		}
		hiveCmd = "use " + input.getDatabase() + ";" + hiveCmd;

		boolean storeResultToFile = input.isStoreResult();
		String querId = input.getQueryid();
		int limit = input.getResultLimit();

		if (StringUtils.isEmpty(username)) {
			logger.error("Input username is empty!");
			return null;
		}

		String statusLocation = getStatusFileLocation(input.getQueryid());
		logger.info("statusLocation:" + statusLocation);
		String resultLocation = input.getResultLocation();
		logger.info("statusLocation:" + resultLocation);

		IStreamHandler resultHandler = null;

//		if (storeResultToFile) {
			resultHandler = StreamHandlerFactory
					.createFileResultHandler(resultLocation);
//		} else {
//			resultHandler = StreamHandlerFactory.createMemResultHandler();
//			
//		}
		resultHandler.setShowLimit(limit);

		String cmd = joinString("bash -c \"",
				"hive --hiveconf hive.cli.print.header=true -e \\\"", hiveCmd,
				"\\\"\"");
		// execute cmd
		int exitCode = -1;
		try {
			exitCode = ShellCmdExecutor.getInstance().execute(cmd, querId,
					resultHandler, statusLocation);
		} catch (ShellCmdExecException e) {
			logger.error("Exceptions occurs in executing hive command!", e);
			return null;
		}
		if (exitCode != 0) {
			logger.error("Hive Command is NOT executed successfully! The exit code of hive command is "
					+ exitCode);
			return null;
		}

		HiveQueryOutputBo res = resultHandler.getResult();
		
		// remove data result file if user didn't request to store
		if (storeResultToFile) {
			res.setResultFileAbsolutePath(resultLocation);
		}else {
			FileUtils.deleteQuietly(new File(resultLocation));
		}
		return res;
	}

	@Override
	public HiveQueryOutput getQueryResult(HiveQueryInput input) {
		return null;
	}

	@Override
	public String getQueryStatus(String queryId) {
		String statusFileLocation = getStatusFileLocation(queryId);
		try {
			return FileUtils.readFileToString(new File(statusFileLocation),
					BasicUtils.ENCODING);
		} catch (IOException e) {
			logger.error("Exception occurs in reading status file: "
					+ statusFileLocation, e);
			return null;
		}
	}

	private static String getStatusFileLocation(String queryId) {
		String statusFileLocation = joinLocation(
				DataFileStore.QUERY_STATUS_LOCATION, queryId + ".stat");
		return statusFileLocation;
	}

	private static String joinString(String... strings) {
		return BasicUtils.joinString(strings);
	}

	private static String joinLocation(String... locationParts) {
		return BasicUtils.joinLocation(locationParts);
	}

	@Override
	public Boolean stopQuery(String queryId) {
		try {
			ShellCmdExecutor.getInstance().stopRunningTask(queryId);
		} catch (Exception e) {
			logger.error("The running query for queryId: " + queryId
					+ "is NOT found!", e);
			return false;
		}
		// stop submitted hadoop job
		File statusFile = new File(getStatusFileLocation(queryId));
		if (statusFile.exists() && statusFile.canRead()) {
			try {
				LineIterator it = BasicUtils.lineIterator(statusFile,
						BasicUtils.ENCODING, false);
				while (it.hasNext()) {
					String line = it.next().toString().trim();
					if (StringUtils.isEmpty(line)) {
						continue;
					}
					if (line.startsWith(KILL_COMMAND_PREFIX)) {
						String killCommand = line.substring(
								KILL_COMMAND_PREFIX_LENGTH).trim();
						logger.info("Kill Command:" + killCommand);
						Runtime run = Runtime.getRuntime();
						Process p = run.exec(killCommand);
						if (p.waitFor() != 0) {
							logger.error("Kill Hadoop Job Failed, exitcode :"
									+ p.exitValue());
						} else {
							logger.info("Kill Hadoop Job Succeed");
						}
					}
				}
			} catch (Exception e) {
				logger.error(
						"Exception Occurs in stopping running hadoop task!", e);
				return false;
			}
		}
		return true;
	}
}
