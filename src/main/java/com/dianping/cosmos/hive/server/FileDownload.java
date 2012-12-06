package com.dianping.cosmos.hive.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.queryengine.jdbc.DataFileStore;

public class FileDownload extends HttpServlet {
	private static final Log logger = LogFactory.getLog(FileDownload.class);
	
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		String filename = URLDecoder.decode(request.getPathInfo(), "UTF-8");
		logger.info("request filename:" + filename);
		
		URI u = URI.create(DataFileStore.FILE_STORE_DIRECTORY_LOCATION);
		File file = new File(u.getPath(), filename);
		response.setContentType("application/x-download");
		response.setHeader("Content-Disposition", "attachment; filename="
				+ filename);
		response.setHeader("Content-Length", String.valueOf(file.length()));

		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try {
			input = new BufferedInputStream(new FileInputStream(file));
			output = new BufferedOutputStream(response.getOutputStream());

			byte[] buffer = new byte[8192];
			for (int length = 0; (length = input.read(buffer)) > 0;) {
				output.write(buffer, 0, length);
			}
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException ioe) {
					logger.error(ioe);
				}
			if (input != null)
				try {
					input.close();
				} catch (IOException ioe) {
					logger.error(ioe);
				}
		}
	}
}
