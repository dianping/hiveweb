package com.dianping.cosmos.hive.server.rijndael;

import java.util.Random;

public class Pass {
	private final static String MATRIX="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final static Integer LENGTH_PASS = 9;
	private final static String PREFIX_PASS = "dp!@";
	private final static String SUFFIX_PASS = "";
	
	public static String pass(Integer length_pass){
		if(length_pass <= 0 )
			return "";
		StringBuffer sb = new StringBuffer();
		Random rand = new Random();
		while(sb.length() != length_pass){
			sb.append(MATRIX.charAt(rand.nextInt(MATRIX.length())));
		}
		return PREFIX_PASS+sb.toString()+SUFFIX_PASS;
	}
	
	public static String pass(){
		return pass(LENGTH_PASS);
	}
}
