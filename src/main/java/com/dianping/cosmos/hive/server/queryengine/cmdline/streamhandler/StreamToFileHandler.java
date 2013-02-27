package com.dianping.cosmos.hive.server.queryengine.cmdline.streamhandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.client.bo.HiveQueryOutputBo;
import com.dianping.cosmos.hive.server.queryengine.cmdline.BasicUtils;
import com.dianping.cosmos.hive.server.queryengine.cmdline.OutputParser;
import com.dianping.cosmos.hive.server.queryengine.jdbc.DataFileStore;

class StreamToFileHandler extends BaseStreamHandler {

	private static final Log logger = LogFactory
			.getLog(StreamToFileHandler.class);

	private String outFileLocation;
	private int fileStoreLineLimit;
	private boolean gzip;

	public StreamToFileHandler(String outFileLocation, int fileStoreLineLimit,
			boolean gzip) {
		this.outFileLocation = outFileLocation;
		this.fileStoreLineLimit = fileStoreLineLimit;
		this.gzip = gzip;

		super.setProcessKillStatus(false);
		try {
			new File(outFileLocation).createNewFile();
		} catch (IOException e) {
			logger.error("Failed to create file:" + outFileLocation, e);
		}
	}

	public StreamToFileHandler(String outFileLocation, boolean gzip) {
		this(outFileLocation, DataFileStore.FILE_STORE_LINE_LIMIT, gzip);
	}

	public void run() {
		if (is == null || outFileLocation == null)
			return;

		LineIterator it = null;
		OutputStream os = null;
		BufferedOutputStream bops = null;
		try {
			it = IOUtils.lineIterator(is, BasicUtils.ENCODING);
			File outFile = new File(outFileLocation);
			FileUtils.touch(outFile);
			os = BasicUtils.openOutputStream(new File(outFileLocation), true, gzip);
			bops = new BufferedOutputStream(os);
			int lineNum = 0;
			while (it.hasNext() && lineNum++ < fileStoreLineLimit) {
				String line = it.nextLine();
				bops.write(line.getBytes());
				bops.write(10);
			}
			
			if (lineNum >= fileStoreLineLimit) {
				logger.info("start to manifest process kill status and destroy main process ");
				super.setProcessKillStatus(true);
				// finally destroy the hive process
				super.proc.destroy();
			}
		} catch (IOException ios) {
			logger.error("Exception occurs in writing data to file:", ios);
		} finally {
			try {
                if (bops != null) {
                	bops.flush();
                	bops.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
			IOUtils.closeQuietly(os);
			it.close();
			IOUtils.closeQuietly(is);
		}
	}

	public HiveQueryOutputBo getResult() {
		try {
			return OutputParser.getInstance()
					.parse(BasicUtils.openInputStream(
							new File(outFileLocation), gzip), showLimit);
		} catch (IOException e) {
			logger.error("Exception occurs in parsing result file : " + outFileLocation, e);
			return null;
		}
	}
}
