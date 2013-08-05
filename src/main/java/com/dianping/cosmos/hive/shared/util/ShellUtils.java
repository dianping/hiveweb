package com.dianping.cosmos.hive.shared.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class ShellUtils {
	private static final Log LOG = LogFactory.getLog(ShellUtils.class);

	public static final boolean WINDOWS = System.getProperty("os.name")
			.startsWith("Windows");

	public static String[] getGroupsForUserCommand(final String user) {
		return new String[] { "bash", "-c", "id -Gn " + user };
	}

	public static List<String> getUnixGroups(final String user)
			throws IOException {
		String result = "";
		try {
			result = ShellUtils.execCommand(ShellUtils
					.getGroupsForUserCommand(user));
		} catch (ExitCodeException e) {
			LOG.warn("got exception trying to get groups for user " + user, e);
		}

		StringTokenizer tokenizer = new StringTokenizer(result);
		List<String> groups = new LinkedList<String>();
		while (tokenizer.hasMoreTokens()) {
			groups.add(tokenizer.nextToken());
		}
		return groups;
	}

	public static String execCommand(String[] cmd) throws IOException {
		String[] commands = cmd.clone();
		ProcessBuilder builder = new ProcessBuilder(commands);
		Process process = builder.start();

		final BufferedReader errReader = new BufferedReader(
				new InputStreamReader(process.getErrorStream()));
		final BufferedReader inReader = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

		final StringBuffer errMsg = new StringBuffer();

		Thread errThread = new Thread() {
			@Override
			public void run() {
				try {
					String line = errReader.readLine();
					while ((line != null) && !isInterrupted()) {
						errMsg.append(line);
						errMsg.append(System.getProperty("line.separator"));
						line = errReader.readLine();
					}
				} catch (IOException ioe) {
					LOG.warn("Error reading the error stream", ioe);
				}
			}
		};

		try {
			errThread.start();
		} catch (IllegalStateException ise) {
			LOG.warn("Error executing error Thread" + ise);
		}

		StringBuffer output = null;
		int exitCode = 0;

		try {
			output = parseExecResult(inReader);
			String line = inReader.readLine();
			while (line != null) {
				line = inReader.readLine();
			}
			// wait for the process to finish and check the exit code
			exitCode = process.waitFor();
			try {
				errThread.join();
			} catch (InterruptedException ie) {
				LOG.warn("Interrupted while reading the error stream", ie);
			}
			if (exitCode != 0) {
				throw new ExitCodeException(exitCode, errMsg.toString());
			}
		} catch (InterruptedException ie) {
			throw new IOException(ie.toString());
		} finally {
			try {
				inReader.close();
			} catch (IOException ioe) {
				LOG.warn("Error while closing the input stream", ioe);
			}
			if (errThread.isAlive()) {
				errThread.interrupt();
			}
			try {
				errReader.close();
			} catch (IOException ioe) {
				LOG.warn("Error while closing the error stream", ioe);
			}
			process.destroy();
		}

		return (output == null) ? "" : output.toString();
	}

	public static StringBuffer parseExecResult(BufferedReader lines)
			throws IOException {
		StringBuffer output = new StringBuffer();
		char[] buf = new char[512];
		int nRead;
		while ((nRead = lines.read(buf, 0, buf.length)) > 0) {
			output.append(buf, 0, nRead);
		}
		return output;
	}
	
	public static class ExitCodeException extends IOException {
		private static final long serialVersionUID = 1L;
		int exitCode;
	    
	    public ExitCodeException(int exitCode, String message) {
	      super(message);
	      this.exitCode = exitCode;
	    }
	    
	    public int getExitCode() {
	      return exitCode;
	    }
	  }

}
