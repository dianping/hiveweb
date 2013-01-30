package com.dianping.cosmos.hive.server;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dianping.cosmos.hive.server.rijndael.Rijndael_Properties;
import com.dianping.cosmos.hive.server.rijndael.Rijndael_Util;

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(Login.class);

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String tt = Rijndael_Util.decode(Rijndael_Properties.getStrKey(""), "");
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String loginid = "raXm84i2%2FaWdehDw%2BLmFO%2BDhi9cu%2F4P%2BM2rndIyzYfzgZoIleU37HA%3D%3D&amp";
		try {
			String tt = Rijndael_Util.decode(Rijndael_Properties.getStrKey("Rijndael_Login_Hive"), "");
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
