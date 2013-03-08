package com.dianping.cosmos.hive.server.queryengine.jdbc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.hadoop.util.ReflectionUtils;

public class DataFileStore {
	private static final Log logger = LogFactory.getLog(DataFileStore.class);

	public final static int DEFAULT_FILE_STORE_LINE_LIMIT = 100000;
	private static DataFileStore instance;

	public final static String FILE_STORE_DIRECTORY_LOCATION;
	public final static String QUERY_STATUS_LOCATION;
	public final static String UPLOAD_DIR_LOCATION;
	public final static int FILE_STORE_LINE_LIMIT;
	public final static String DEFAULT_CODEC_CLASS = "org.apache.hadoop.io.compress.GzipCodec";
	public final static int BUFFER_SIZE = 8 * 1024;
	public final static String ENCODING = "utf-8";
	public final static String FILE_EXTENSION = "xls.gz";

	static {
		ResourceBundle bundle = ResourceBundle.getBundle("context");
		FILE_STORE_DIRECTORY_LOCATION = bundle
				.getString("hive-web.store.data.location");
		QUERY_STATUS_LOCATION = bundle
				.getString("hive-web.store.query.status.location");
		UPLOAD_DIR_LOCATION = bundle
				.getString("hive-web.store.upload.location");
		FILE_STORE_LINE_LIMIT = tryParseInt(
				bundle.getString("hive-web.store.data.file.line.limit"),
				DEFAULT_FILE_STORE_LINE_LIMIT);
	}

	public static DataFileStore getInstance() {
		if (instance == null) {
			synchronized (DataFileStore.class) {
				if (instance == null) {
					instance = new DataFileStore();
					logger.info("initialize DataFileStore, FILE_STORE_DIRECTORY_LOCATION is "
							+ FILE_STORE_DIRECTORY_LOCATION
							+ " FILE_STORE_LINE_LIMIT is "
							+ FILE_STORE_LINE_LIMIT
							+ " QUERY_STATUS_LOCATION is "
							+ QUERY_STATUS_LOCATION
							+ " UPLOAD_DIR_LOCATION"
							+ UPLOAD_DIR_LOCATION);
				}
			}
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

	public static BufferedWriter openOutputStream(String file)
			throws IOException {
		Configuration conf = new Configuration();
		URI uri = null;
		try {
			uri = new URI(file);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("file:" + file, e);
		}
		FileSystem fs = FileSystem.get(uri, conf);

		Class<?> codecClass = null;
		try {
			codecClass = Class.forName(DEFAULT_CODEC_CLASS);
		} catch (ClassNotFoundException e) {
			logger.error(DEFAULT_CODEC_CLASS + " not found", e);
		}
		CompressionCodec codec = (CompressionCodec) ReflectionUtils
				.newInstance(codecClass, conf);

		if (logger.isDebugEnabled()) {
			logger.debug("reflection using compression codec class: "
					+ codec.getClass().getName());
		}
		FSDataOutputStream out = fs.create(new Path(file), false, BUFFER_SIZE);
		CompressionOutputStream co = codec.createOutputStream(out);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(co,
				ENCODING), BUFFER_SIZE);

		return bw;
	}

	public static String getStoreFileAbsolutePath(String tokenid,
			String username, String database, String hql, long timestamp,
			String queryId) {
		String fileName = getStoreFileName(tokenid, username, database, hql,
				timestamp, queryId);
		StringBuilder sb = new StringBuilder();
		sb.append(FILE_STORE_DIRECTORY_LOCATION).append(File.separator)
				.append(fileName).append(".").append(FILE_EXTENSION);
		return sb.toString();
	}

	public static String getStoreFileName(String tokenid, String username,
			String database, String hql, long timestamp, String queryId) {
		String input = tokenid + username + database + hql + timestamp
				+ queryId;
		String md5Filename = getMD5Hash(input);
		return md5Filename;
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
