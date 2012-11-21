package com.dianping.cosmos.hive.server.queryengine.jdbc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DataFileStore {
	private static final Log logger = LogFactory.getLog(DataFileStore.class);

	private static int DEFAULT_FILE_STORE_LINE_LIMIT = 10000;
	private static DataFileStore instance;

	public static String FILE_STORE_DIRECTORY_LOCATION;
	public static int FILE_STORE_LINE_LIMIT;

	static {
		ResourceBundle bundle = ResourceBundle.getBundle("context");
		FILE_STORE_DIRECTORY_LOCATION = bundle
				.getString("hive-web.store.data.location");
		FILE_STORE_LINE_LIMIT = tryParseInt(
				bundle.getString("hive-web.store.data.file.line.limit"),
				DEFAULT_FILE_STORE_LINE_LIMIT);
	}

	public static DataFileStore getInstance() {
		if (instance == null) {
			instance = new DataFileStore();
			logger.info("initialize DataFileStore, FILE_STORE_DIRECTORY_LOCATION is "
					+ FILE_STORE_DIRECTORY_LOCATION
					+ " FILE_STORE_LINE_LIMIT is " + FILE_STORE_LINE_LIMIT);
		}
		return instance;
	}

	private static int tryParseInt(String value, int defaultValue) {
		int val;
		try {
			val = Integer.parseInt(value);
		} catch (NumberFormatException nfe) {
			logger.error("parse String value to int failed, instead using default value "
					+ nfe);
			val = defaultValue;
		}
		return val;
	}

	public static OutputStream openOutputStream(File file, boolean gzip)
			throws IOException {
		if (file.exists()) {
			logger.info("file " + file + "exists, hiveweb now delete it");
			if (file.delete()) {
				logger.info("successfully delete file " + file);
			} else {
				logger.error("failed to delete file " + file);
			}
		}
		file.createNewFile();
		if (gzip) {
			return new GZIPOutputStream(new FileOutputStream(file));
		}
		return new FileOutputStream(file);
	}
	
	public static String getStoreFilePath(String tokenid, String username,
			String database, String hql, long timestamp){
		String input = tokenid + username + database + hql + timestamp;
		String md5 = getMD5Hash(input);
		return String.format("%s/%s.gz", FILE_STORE_DIRECTORY_LOCATION, md5);
	}

	private static String getMD5Hash(String input) {
		String md5 = null;
		if (null == input)
			return null;
		try {
			// Create MessageDigest object for MD5
			MessageDigest digest = MessageDigest.getInstance("MD5");
			// Update input string in message digest
			digest.update(input.getBytes(), 0, input.length());
			// Converts message digest value in base 16 (hex)
			md5 = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return md5;
	}
}
