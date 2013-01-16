package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.IStreamHandler;

public class ShellCmdExecutor {
	private static final Log logger = LogFactory.getLog(ShellCmdExecutor.class);

	private static ShellCmdExecutor instance;
	private static final String TMP_DIR = "/tmp";
	private AtomicLong sequenceNumber = new AtomicLong(0);
	private Map<String, Process> runningTaskMap = new ConcurrentHashMap<String, Process>();

	private ShellCmdExecutor() {
	}

	public static ShellCmdExecutor getInstance() {
		if (instance == null) {
            synchronized (ShellCmdExecutor.class) {
                if (instance == null) {
                    instance = new ShellCmdExecutor();
                }
            }
        }
        return instance;
	}

	public final int execute(String cmd, String queryId,
			IStreamHandler outputHandler, String statusLocation)
			throws ShellCmdExecException {
		String cmdLocation = BasicUtils.joinLocation(TMP_DIR, "hive_script_"
				+ sequenceNumber.getAndIncrement() + ".sh");
		try {
			File cmdFile = new File(cmdLocation);
			FileUtils.touch(cmdFile);
			cmdFile.setExecutable(true, false);
			BasicUtils.writeStringToFile(cmdFile, cmd, BasicUtils.ENCODING,
					false, false);

			int exitCode = executeInternals(queryId, outputHandler,
					statusLocation, cmdLocation);
			FileUtils.deleteQuietly(cmdFile);
			return exitCode;
		} catch (Exception e) {
			throw new ShellCmdExecException(e);
		}
	}

	public final void stopRunningTask(String queryId)
			throws ShellCmdExecException, IOException {
		Process proc = runningTaskMap.get(queryId);
		if (proc == null) {
			throw new ShellCmdExecException(
					"Failed to get queryId from runningTaskMap when try to stop running task");
		}
		runningTaskMap.remove(queryId);
		proc.getInputStream().close();
		proc.getErrorStream().close();
		proc.destroy();
	}

	private final int executeInternals(String queryId,
			IStreamHandler outputHandler, String statusLocation, String... cmd)
			throws IOException, InterruptedException, ShellCmdExecException {
		return executeInternals(outputHandler, statusLocation,
				new ProcessBuilder(cmd), queryId);
	}

	private final int executeInternals(IStreamHandler outputHandler,
			final String statusLocation, ProcessBuilder pb, String queryId)
			throws IOException, InterruptedException, ShellCmdExecException {
		final Process proc = pb.start();
		if (queryId != null) {
			runningTaskMap.put(queryId, proc);
		}

		if (outputHandler != null) {
			outputHandler.setInputStream(proc.getInputStream());
			outputHandler.setExecuteProcess(proc);
			Thread t = new Thread(outputHandler);
			t.setDaemon(true);
			t.start();
		}

		if (!StringUtils.isEmpty(statusLocation)) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						File statusFile = new File(statusLocation);
						statusFile.deleteOnExit();
						FileUtils.touch(statusFile);
						OutputStream os = new FileOutputStream(statusFile, true);
						InputStream is = proc.getErrorStream();
						IOUtils.copy(is, os);
						IOUtils.closeQuietly(os);
						IOUtils.closeQuietly(is);
					} catch (IOException e) {
						logger.error("error occured when dump errorstream to status file", e);
					}

				}
			});
			t.setDaemon(true);
			t.start();
		}
		int exitCode = proc.waitFor();
		if (runningTaskMap.containsKey(queryId)) {
			runningTaskMap.remove(queryId);
		}

		/*
		 * if the hive process was killed when it has exceeded the data file
		 * line limit, it won't be an error, so return normal termination exit
		 * code 0
		 */
		if (outputHandler.getProcessKillStatus()) {
			logger.info("process was killed because of exceedance of data file line limit");
			return 0;
		}
		return exitCode;
	}
}
