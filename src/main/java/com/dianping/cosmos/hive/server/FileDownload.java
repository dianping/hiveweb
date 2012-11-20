package com.dianping.cosmos.hive.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileDownload extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String DOWNLOAD_DIRECTORY = "/data/hive-web-download-data";

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		System.out
				.println("-------------------\n-----------\n-----------\n----------");

		String filename = URLDecoder.decode(request.getPathInfo(), "UTF-8");
		File file = new File(DOWNLOAD_DIRECTORY, filename);
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
				} catch (IOException ignore) {
				}
			if (input != null)
				try {
					input.close();
				} catch (IOException ignore) {
				}
		}
	}

}
