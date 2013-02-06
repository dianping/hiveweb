package com.dianping.cosmos.hive.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.security.Krb5Login;
import org.apache.hadoop.security.UserGroupInformation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import sun.security.krb5.internal.tools.kinit;

import com.dianping.cosmos.hive.server.queryengine.jdbc.HiveJdbcClient;
import com.dianping.cosmos.hive.server.rijndael.Rijndael_Util;

public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Log logger = LogFactory.getLog(Login.class);

	private static final int HALD_DAY_IN_SECONDS = 12 * 60 * 60;
	private static final String LOGIN_KEY = "dp!@hiveLogin";
	private static final String GET_PASSWORD_URL = "http://192.168.7.204:8080/pluto/json/getUserInfoPasswordForHive?";
	private static final String HOME_PAGE = "http://10.1.77.84:8080/home.html";

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			String loginid = Rijndael_Util.decode(LOGIN_KEY,
					request.getParameter("loginId"));
			String name = Rijndael_Util.decode(LOGIN_KEY,
					request.getParameter("name"));
			String userGroupid = Rijndael_Util.decode(LOGIN_KEY,
					request.getParameter("userGroupId"));
			logger.info("login from data.dp, loginid: " + loginid + " ;name: "
					+ name + " ;userGroupid: " + userGroupid);

			User user = getUserInfo(loginid, userGroupid);
			String groupUsername = user.getUsername();
			String groupPasswd = user.getPasswd();
			logger.info("groupUsername: " + groupUsername + " ;groupPasswd: "
					+ groupPasswd);

			try {
				kinit.createTicket(groupUsername, groupPasswd, "/tmp/"
						+ groupUsername + ".ticketcache");
			} catch (Exception e) {
				logger.error("kinit create ticket cache failed:" + e);
				return;
			}
			
			UserGroupInformation ugi = Krb5Login.getVerifiedUgi(groupUsername,
					groupPasswd);
			if (ugi == null) {
				logger.error(String.format(
						"Get Verified UGI Failed, username:%s ,password:%s",
						groupUsername, groupPasswd));
			} else {
				// Test if this ugi can work through hive server authentication
				Connection conn = HiveJdbcClient.getConnection(ugi);
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						logger.error(e);
					}
					String tokenid = UUID.randomUUID().toString();
					Date addtime = new Date();

					LoginServiceImpl.getTokenCache().put(tokenid, addtime);
					HiveJdbcClient.putUgiCache(tokenid, ugi);

					Cookie ckName = new Cookie("username", groupUsername);
					Cookie ckRealuser = new Cookie("realuser", name);
					Cookie ckToken = new Cookie("tokenid", tokenid);
					Cookie ckAddtime = new Cookie("addtime", addtime.toString());

					ckName.setMaxAge(HALD_DAY_IN_SECONDS);
					ckRealuser.setMaxAge(HALD_DAY_IN_SECONDS);
					ckToken.setMaxAge(HALD_DAY_IN_SECONDS);
					ckAddtime.setMaxAge(HALD_DAY_IN_SECONDS);

					response.addCookie(ckName);
					response.addCookie(ckRealuser);
					response.addCookie(ckToken);
					response.addCookie(ckAddtime);
					response.sendRedirect(HOME_PAGE);

					if (logger.isDebugEnabled()) {
						Map<String, Date> tokenCacheSnapshot = LoginServiceImpl
								.getTokenCache().asMap();
						for (Map.Entry<String, Date> t : tokenCacheSnapshot
								.entrySet()) {
							logger.info("tokenid:" + t.getKey() + " addtime:"
									+ t.getValue());
						}
					}
					logger.info(String
							.format("Connection established successfully , username:%s , password:%s ,tokenid:%s ,tokenCacheSize:%s",
									groupUsername, groupPasswd, tokenid,
									LoginServiceImpl.getTokenCache().size()));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static User getUserInfo(String loginid, String userGroupid)
			throws IOException, ParseException {
		User u = null;
		HttpURLConnection urlConnection = null;
		StringBuilder sb = new StringBuilder(GET_PASSWORD_URL);
		sb.append("login_id=").append(loginid).append("&user_id=")
				.append(userGroupid).append("&token=xiiqwpbtanvgcnafmnfw");
		URL url = new URL(sb.toString());
		urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setRequestMethod("GET");
		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		urlConnection.connect();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				urlConnection.getInputStream(), "utf-8"));

		sb.delete(0, sb.length());
		String lines;
		while ((lines = reader.readLine()) != null) {
			sb.append(lines);
		}
		JSONParser parser = new JSONParser();
		Object o = parser.parse(sb.toString());
		JSONObject jsonObject = (JSONObject) o;
		Long code = (Long) jsonObject.get("code");
		if (code == 200) {
			u = new User();
			JSONObject msg = (JSONObject) jsonObject.get("msg");
			u.setUsername((String) msg.get("username"));
			u.setPasswd((String) msg.get("passwd"));
		}
		urlConnection.disconnect();
		return u;
	}

	static class User {
		private String username;
		private String passwd;

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPasswd() {
			return passwd;
		}

		public void setPasswd(String passwd) {
			this.passwd = passwd;
		}
	}

}
