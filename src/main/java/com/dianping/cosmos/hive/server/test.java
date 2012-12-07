package com.dianping.cosmos.hive.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.hive.cli.CliSessionState;
import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.ql.CommandNeedRetryException;
import org.apache.hadoop.hive.ql.Driver;
import org.apache.hadoop.hive.ql.processors.CommandProcessor;
import org.apache.hadoop.hive.ql.processors.CommandProcessorFactory;
import org.apache.hadoop.hive.ql.session.SessionState;

public class test {
	protected static final Log l4j = LogFactory.getLog(test.class.getName());

	private static ArrayList<ArrayList<String>> resultBucket;
	private static int resultBucketMaxSize = 100;

	public static void main(String[] args) {
		try {
			PrintStream ps = new PrintStream(
					new File("/tmp/hivelog.out.txt"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Integer> queryRet = new ArrayList<Integer>(); 
		HiveConf conf = new HiveConf(SessionState.class);
		CliSessionState ss = new CliSessionState(conf);
		SessionState.start(ss);

		String cmd = "show tables";
		String cmd_trimmed = cmd.trim();
		String[] tokens = cmd_trimmed.split("\\s+");
		String cmd_1 = cmd_trimmed.substring(tokens[0].length()).trim();

		CommandProcessor proc = CommandProcessorFactory.get(tokens[0]);
		if (proc != null) {
			if (proc instanceof Driver) {
				Driver qp = (Driver) proc;
				qp.setTryCount(Integer.MAX_VALUE);
				try {
					queryRet.add(Integer.valueOf(qp.run(cmd).getResponseCode()));
					ArrayList<String> res = new ArrayList<String>();
					try {
						while (qp.getResults(res)) {
							ArrayList<String> resCopy = new ArrayList<String>();
							resCopy.addAll(res);
							resultBucket.add(resCopy);
							if (resultBucket.size() > resultBucketMaxSize) {
								resultBucket.remove(0);
							}
							for (String row : res) {
								if (ss != null) {
									if (ss.out != null) {
										ss.out.println(row);
									}
								} else {
									throw new RuntimeException("ss was null");
								}
							}
							res.clear();
						}

					} catch (IOException ex) {
						l4j.error(ex);
					}
				} catch (CommandNeedRetryException e) {
				} finally {
					qp.close();
				}
			} else {
				// try {
				// queryRet.add(Integer.valueOf(proc.run(cmd_1).getResponseCode()));
				// } catch (CommandNeedRetryException e) {
				// }
			}
		} else {
		}

	}

}
