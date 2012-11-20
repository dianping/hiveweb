package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler.IStreamHandler;

public class ShellCmdExecutor {
	
	private static ShellCmdExecutor s_instance = new ShellCmdExecutor();
	private static final String TMP_DIR = "/tmp";
	private AtomicLong sequenceNumber = new AtomicLong(0);
	private Map<String, Process> runningTaskMap = new ConcurrentHashMap<String, Process>();
	
	
	private ShellCmdExecutor(){}
	
	public static ShellCmdExecutor getInstance(){
		return s_instance;
	}
	
	public final int execute(String cmd, String key, IStreamHandler outputHandler, IStreamHandler errorHandler) throws ShellCmdExecException{
		String cmdFile = BasicUtils.joinString("script_", ""+sequenceNumber.getAndIncrement(), ".sh");
		String cmdLocation = BasicUtils.joinLocation(TMP_DIR, cmdFile);
		try{
			BasicUtils.writeStringToFile(new File(cmdLocation), cmd, BasicUtils.ENCODING, false, false);
			//add execution privilege to cmd File
			executeInternals("chmod", "777", cmdLocation);
			//execute cmd
			int exitCode = executeInternals(key, outputHandler, errorHandler, cmdLocation);
			new File(cmdLocation).delete();
			return exitCode;
		} catch(Exception e){
			throw new ShellCmdExecException(e);
		}
	}
	
	public final void stopRunningTask(String key) throws ShellCmdExecException, IOException{
		Process proc = runningTaskMap.get(key);
		if(proc == null)
			throw new ShellCmdExecException(null);
		proc.getInputStream().close();
		proc.getErrorStream().close();
		proc.destroy();
		runningTaskMap.remove(key);
	}

	private final int executeInternals(String... cmd) throws IOException, InterruptedException, ShellCmdExecException{
		return executeInternals(null, null, null, cmd);
	}

	private final int executeInternals(String key, IStreamHandler outputHandler, IStreamHandler errorHandler, String... cmd) throws IOException, InterruptedException, ShellCmdExecException{
		return executeInternals(outputHandler, errorHandler, new ProcessBuilder(cmd), key);
	}

	private final int executeInternals(IStreamHandler outputHandler, IStreamHandler errorHandler, ProcessBuilder pb, String key) throws IOException, InterruptedException, ShellCmdExecException{
		Process proc = pb.start();
		if(key != null){
			runningTaskMap.put(key, proc);
		}
		if(outputHandler != null){
			outputHandler.setInputStream(proc.getInputStream());
			Thread t = new Thread(outputHandler);
			t.setDaemon(true);
			t.start();
		}
		if(errorHandler != null){
			errorHandler.setInputStream(proc.getErrorStream());
			Thread t = new Thread(errorHandler);
			t.setDaemon(true);
			t.start();
		}

		int exitCode = proc.waitFor();
		Thread.sleep(1000);
		runningTaskMap.remove(key);
		return exitCode;
	}

}
