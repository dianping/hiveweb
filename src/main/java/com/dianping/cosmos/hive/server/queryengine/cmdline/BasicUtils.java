package com.dianping.cosmos.hive.server.queryengine.cmdline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class BasicUtils {

	public static final String ENCODING = "utf-8";
	
	private BasicUtils(){}

	public static String joinString(String... strings){
		StringBuilder sb = new StringBuilder();
		for(String s : strings){
			sb.append(s);
		}
		return sb.toString();
	}

	public static String joinLocation(String... locationParts){
		StringBuilder sb = new StringBuilder();

		for(int i = 0; i < locationParts.length; i++){
			String locationPart = locationParts[i];
			sb.append(locationPart);
			if(i != locationParts.length -1 && !locationPart.endsWith(File.separator))
				sb.append(File.separator);
		}
		return sb.toString();
	}

	public static void writeByteArrayToFile(File file, byte[] data, boolean append, boolean gzip) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append, gzip);
			out.write(data);
			out.close(); // don't swallow close Exception if copy completes normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static void writeStringToFile(File file, String data, String encoding, boolean append, boolean gzip) throws IOException {
		OutputStream out = null;
		try {
			out = openOutputStream(file, append, gzip);
			IOUtils.write(data, out, encoding);
			out.close(); // don't swallow close Exception if copy completes normally
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	public static LineIterator lineIterator(File file, String encoding, boolean gzip) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file, gzip);
			return IOUtils.lineIterator(in, encoding);
		} catch (IOException ex) {
			IOUtils.closeQuietly(in);
			throw ex;
		} catch (RuntimeException ex) {
			IOUtils.closeQuietly(in);
			throw ex;
		}
	}

	private static OutputStream openOutputStream(File file, boolean append, boolean gzip) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canWrite() == false) {
				throw new IOException("File '" + file + "' cannot be written to");
			}
		} else {
			File parent = file.getParentFile();
			if (parent != null) {
				if (!parent.mkdirs() && !parent.isDirectory()) {
					throw new IOException("Directory '" + parent + "' could not be created");
				}
			}
		}

		if(gzip)
			return new GZIPOutputStream(new FileOutputStream(file, append));

		return new FileOutputStream(file, append);
	}

	public static InputStream openInputStream(File file, boolean gzip) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		if(gzip)
			return new GZIPInputStream(new FileInputStream(file));

		return new FileInputStream(file);
	}
}
