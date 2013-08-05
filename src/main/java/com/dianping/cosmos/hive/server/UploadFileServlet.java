package com.dianping.cosmos.hive.server;

import static gwtupload.shared.UConsts.PARAM_SHOW;
import gwtupload.server.UploadAction;
import gwtupload.server.exceptions.UploadActionException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.util.Shell.ShellCommandExecutor;

import com.dianping.cosmos.hive.server.queryengine.jdbc.DataFileStore;

public class UploadFileServlet extends UploadAction {
	private static final long serialVersionUID = 1L;

	Hashtable<String, String> receivedContentTypes = new Hashtable<String, String>();
	/**
	 * Maintain a list with received files and their content types.
	 */
	Hashtable<String, File> receivedFiles = new Hashtable<String, File>();

	/**
	 * Override executeAction to save the received files in a custom place and
	 * delete this items from session.
	 */
	@Override
	public String executeAction(HttpServletRequest request,
			List<FileItem> sessionFiles) throws UploadActionException {
		int cont = 0;
		String username = StringUtils.EMPTY;
		Cookie[] cookies = request.getCookies();
		for (Cookie c : cookies){
			if ("username".equalsIgnoreCase(c.getName())){
				username = c.getValue();
			}
		}
		if (StringUtils.isEmpty(username)){
			logger.error("request cookies doesn't contain username");
			throw new UploadActionException("request cookies doesn't contain username");
		}
		
		File file = null;
		for (FileItem item : sessionFiles) {
			if (false == item.isFormField()) {
				cont++;
				try {
					file = File.createTempFile(username + "-upload-", ".txt", new File(DataFileStore.UPLOAD_DIR_LOCATION));
					file.deleteOnExit();
					item.write(file);
					// / Save a list with the received files
					receivedFiles.put(item.getFieldName(), file);
					receivedContentTypes.put(item.getFieldName(),
							item.getContentType());
				} catch (Exception e) {
					throw new UploadActionException(e);
				}
			}
		}
		// / Remove files from session because we have a copy of them
		removeSessionFileItems(request);

		// / Send information of the received files to the client.
		return file.getAbsolutePath();
	}

	/**
	 * Get the content of an uploaded file.
	 */
	@Override
	public void getUploadedFile(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String fieldName = request.getParameter(PARAM_SHOW);
		File f = receivedFiles.get(fieldName);
		if (f != null) {
			response.setContentType(receivedContentTypes.get(fieldName));
			FileInputStream is = new FileInputStream(f);
			copyFromInputStreamToOutputStream(is, response.getOutputStream());
		} else {
			renderXmlResponse(request, response, XML_ERROR_ITEM_NOT_FOUND);
		}
	}

	/**
	 * Remove a file when the user sends a delete request.
	 */
	@Override
	public void removeItem(HttpServletRequest request, String fieldName)
			throws UploadActionException {
		File file = receivedFiles.get(fieldName);
		receivedFiles.remove(fieldName);
		receivedContentTypes.remove(fieldName);
		if (file != null) {
			file.delete();
		}
	}
}
